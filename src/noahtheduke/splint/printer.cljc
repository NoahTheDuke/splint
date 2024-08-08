; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.printer
  (:require
   #?(:bb [cheshire.core :as json]
      :clj [clojure.data.json :as json])
   [clojure.main :refer [demunge]]
   [clojure.string :as str]
   [fipp.clojure :as fipp.clj]
   [fipp.visit :as fipp.v])
  (:import
   [java.io StringWriter]))

(set! *warn-on-reflection* true)

(defn pretty-quote [p [macro arg]]
  [:span (case macro
           splint/deref "@"
           splint/fn "#"
           splint/re-pattern "#"
           splint/read-eval "#="
           splint/syntax-quote "`"
           splint/unquote "~"
           splint/unquote-splicing "~@"
           splint/var "#'")
   (fipp.v/visit p arg)])

(def specials
  (reduce-kv
    (fn [m k v]
      (if (#{"deref" "fn" "re-pattern" "read-eval" "syntax-quote"
             "unquote" "unquote-splicing" "var"} (name k))
        m
        (assoc m k v)))
    {'splint/deref pretty-quote
     'splint/fn fipp.clj/pretty-fn*
     'splint/re-pattern pretty-quote
     'splint/read-eval pretty-quote
     'splint/syntax-quote pretty-quote
     'splint/unquote pretty-quote
     'splint/unquote-splicing pretty-quote
     'splint/var pretty-quote}
    fipp.clj/default-symbols))

(defn pprint-str [form]
  (let [s (StringWriter.)]
    (fipp.clj/pprint form {:symbols specials
                           :writer s})
    (str/trim (str s))))

(defmacro print-form [form]
  `(fipp.clj/pprint ~form {:symbols specials}))

(defn st-element->str
  [[class-name method-name file-name line-number]]
  (let [clojure-fn? (and file-name
                      (or (str/ends-with? file-name ".clj")
                        (str/ends-with? file-name ".cljc")
                        (.equals "NO_SOURCE_FILE" file-name)))]
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
    #?(:bb (println (json/generate-string diagnostic))
       :clj (json/write diagnostic *out* {:escape-slash false}))
    (newline)))

(defmethod print-find "json-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                     (update :rule-name pr-str)
                     (update :form pr-str)
                     (update :alt pr-str)
                     (update :filename str)
                     (update :exception update-trace)
                     (->> (into (sorted-map))))]
    #?(:bb (println (json/generate-string diagnostic {:pretty true}))
       :clj (json/pprint diagnostic {:escape-slash false}))
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
    (print-form (into (sorted-map) diagnostic))))

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
