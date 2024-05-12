; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.printer
  (:require
   [clojure.data.json :as json]
   [clojure.main :refer [demunge]]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [noahtheduke.splint.clojure-ext.core :refer [->list postwalk*]]))

(set! *warn-on-reflection* true)

(defn- render-deref [[_ sexp]]
  (symbol (str "@" (print-str sexp))))

(defn- render-fn [[_fn _args sexp]]
  (symbol (str "#" (print-str sexp))))

(defn- render-read-eval [[_ sexp]]
  (symbol (str "#=" (print-str sexp))))

(defn- render-re-pattern [[_ sexp]]
  (symbol (str "#" (pr-str sexp))))

(defn- render-var [[_ sexp]]
  (symbol (str "#'" (print-str sexp))))

(defn- render-syntax-quote [[_ sexp]]
  (symbol (str "`" (print-str sexp))))

(defn- render-unquote [[_ sexp]]
  (symbol (str "~" (print-str sexp))))

(defn- render-unquote-splicing [[_ sexp]]
  (symbol (str "~@" (print-str sexp))))

(defn- uplift [sexp]
  (if (seq? sexp)
    (->> (reduce
           (fn [acc cur]
             (if (::uplift (meta cur))
               (apply conj acc cur)
               (conj acc cur)))
           []
           sexp)
      (->list))
    sexp))

(defn revert-splint-reader-macros [replace-form]
  (postwalk*
    (fn [sexp]
      (if (seq? sexp)
        (if-let [f (case (first sexp)
                     splint/deref #'render-deref
                     splint/fn #'render-fn
                     splint/read-eval #'render-read-eval
                     splint/re-pattern #'render-re-pattern
                     splint/var #'render-var
                     splint/syntax-quote #'render-syntax-quote
                     splint/unquote #'render-unquote
                     splint/unquote-splicing #'render-unquote-splicing
                     ; else
                     nil)]
          (uplift (f sexp))
          sexp)
        sexp))
    replace-form))

(defmacro print-form [form]
  `(pp/pprint (revert-splint-reader-macros ~form)))

(defn st-element->str
  [[class-name method-name file-name line-number]]
  (let [clojure-fn? (and file-name
                      (or (str/ends-with? file-name ".clj")
                        (str/ends-with? file-name ".cljc")
                        (= file-name "NO_SOURCE_FILE")))]
    (str (if clojure-fn?
           (demunge (str class-name))
           (str class-name "." method-name))
      " (" file-name ":" line-number ")")))

(defn st->str
  [st]
  (into []
    (comp
      (remove #(#{'clojure.lang.AFn 'clojure.lang.RestFn} (first %)))
      (dedupe)
      (take 12)
      (map st-element->str))
    st))

(defn print-find-dispatch [output _diagnostic] output)

(defmulti print-find #'print-find-dispatch)

(defmethod print-find "full" [_ {:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (when form
    (print-form form))
  (when alt
    (println "Consider using:")
    (print-form alt))
  (newline))

(defmethod print-find "simple" [_ {:keys [filename rule-name line column message]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline))

(defmethod print-find "clj-kondo" [_ {:keys [filename line column message]}]
  (printf "%s:%s:%s: warning: %s" filename line column message)
  (newline))

(defmethod print-find "markdown" [_ {:keys [filename rule-name form line column message alt]}]
  (println "----")
  (newline)
  (printf "#### %s:%s:%s [%s]" filename line column rule-name)
  (newline)
  (newline)
  (println message)
  (newline)
  (when form
    (println "```clojure")
    (print-form form)
    (println "```")
    (newline))
  (when alt
    (println "Consider using:")
    (newline)
    (println "```clojure")
    (print-form alt)
    (println "```")
    (newline)))

(defn update-trace [ex]
  (when (:trace ex)
    (update ex :trace st->str)))

(defmethod print-find "json" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                     (update :rule-name pr-str)
                     (update :form pr-str)
                     (update :alt pr-str)
                     (update :filename str)
                     (update :exception update-trace)
                     (->> (into (sorted-map))))]
    (json/write diagnostic *out* {:escape-slash false})
    (newline)))

(defmethod print-find "json-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                     (update :rule-name pr-str)
                     (update :form pr-str)
                     (update :alt pr-str)
                     (update :filename str)
                     (update :exception update-trace)
                     (->> (into (sorted-map))))]
    (json/pprint diagnostic {:escape-slash false})
    (newline)))

(defmethod print-find "edn" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                     (update :filename str)
                     (update :exception update-trace))]
    (prn (into (sorted-map) diagnostic))))

(defmethod print-find "edn-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                     (update :filename str)
                     (update :exception update-trace))]
    (pp/pprint (into (sorted-map) diagnostic))))

(defn error-diagnostic [diagnostic]
  (#{'splint/error
     'splint/parsing-error
     'splint/unknown-error} (:rule-name diagnostic)))

(def sort-fn (juxt :filename :line :column))

(defn print-results
  [{:keys [config diagnostics checked-files total-time]}]
  (when-not (or (:quiet config) (:silent config))
    (let [printer (get-method print-find (:output config))
          diagnostics (if (:errors config)
                        (filter error-diagnostic diagnostics)
                        diagnostics)]
      (doseq [diagnostic (sort-by sort-fn diagnostics)]
        (printer nil diagnostic))
      (flush)))
  (when-not (or (:silent config)
              (false? (:summary config))
              (#{"markdown" "json" "json-pretty"
                 "edn" "edn-pretty"} (:output config)))
    (printf "Linting took %sms, checked %s files, %s style warnings%s\n"
      total-time
      (count checked-files)
      (count (remove error-diagnostic diagnostics))
      (if-let [errors (seq (filter error-diagnostic diagnostics))]
        (format ", %s errors" (count errors))
        ""))
    (flush)))
