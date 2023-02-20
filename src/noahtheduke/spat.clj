; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [edamame.core :as e]
   [noahtheduke.spat.pattern :refer [simple-type]]
   [noahtheduke.spat.rules :refer [check-rule grouped-rules]])
  (:import
   (java.io File))
  (:gen-class))

(set! *warn-on-reflection* true)

(def clj-defaults
  {:all true
   :quote true
   :row-key :line
   :col-key :column
   :end-location false
   :location? seq?
   :features #{:cljs}
   :read-cond :preserve
   :auto-resolve (fn [k] (if (= :current k) 'spat (name k)))
   :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "spat" (name r))) v)))})

(defn parse-string [s] (e/parse-string s clj-defaults))
(defn parse-string-all [s] (e/parse-string-all s clj-defaults))

(comment
  (e/parse-string-all
    "::a :a/b #sql/raw [1 2 3] #unknown [4]"
    {:auto-resolve (fn [k] (if (= :current k) 'spat (name k)))
     :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "spat" (name r))) v)))}))

(defn check-multiple-rules [rules form]
  (reduce
    (fn [_ rule]
      (when-let [alt (try (check-rule rule form)
                          (catch Throwable e
                            (throw (ex-info (ex-message e)
                                          (merge {:rule-name (:name rule)
                                                  :form form
                                                  :line (:line (meta form))
                                                  :column (:column (meta form))}
                                                 (ex-data e))
                                          e))))]
        (let [form-meta (meta form)]
          (reduced {:rule-name (:name rule)
                    :form form
                    :line (:line form-meta)
                    :column (:column form-meta)
                    :alt alt}))))
    nil
    rules))

(defn check-rules-for-type [form]
  (when-let [rules (grouped-rules (simple-type form))]
    (check-multiple-rules rules form)))

(defn run!!
  "Reduce over a collection purely for side effects, returning nil. Reducing function
  returns nil to disallow using reduced in the proc."
  [proc coll]
  (reduce (fn [_ cur] (proc cur) nil) nil coll)
  nil)

(defn check-subforms [ctx filename form]
  (let [alt-map (try (check-rules-for-type form)
                     (catch clojure.lang.ExceptionInfo e
                       (throw (ex-info (ex-message e)
                                       (assoc (ex-data e) :filename filename)
                                       e))))]
    (when alt-map
      (swap! ctx update :violations conj (assoc alt-map :filename filename)))
    (when (seqable? form)
      (run!! (fn [fm] (check-subforms filename fm ctx)) form))))

(defn check-file [ctx ^File file]
  (let [parsed-file (try (parse-string-all (slurp file))
                         (catch Throwable e
                           (prn (ex-info (ex-message e)
                                       (assoc (ex-data e) :filename (str file))
                                       e))))
        filename (str file)]
    (run!! (fn [form] (check-subforms ctx filename form)) parsed-file)))

(defn check-directories [ctx dirs]
  (->> (map io/file dirs)
       (mapcat file-seq)
       (set)
       (filter #(and (.isFile ^File %)
                     (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
       (pmap #(check-file ctx %))
       (doall)))

(defn print-find [{:keys [filename rule-name form line column alt]}]
  (printf "[:%s] %s - %s:%s" rule-name filename line column)
  (newline)
  (pprint/pprint form)
  (println "Consider using:")
  (pprint/pprint alt)
  (newline)
  (flush))

(def cli-options
  [
   ["-h" "--help" "This message"]
   [nil "--clj-kondo" "Output in clj-kondo format"
    :default false]
   ["-q" "--quiet" "Print no suggestions, only return exit code"
    :default false]
   ])

(comment
  (parse-opts ["--quiet" "src"] cli-options))

(defn print-help
  [summary]
  (->> ["splint: sexpr pattern matching and idiom checking"
        ""
        "Usage:"
        "  splint [options] [path...]"
        "  splint [options] -- [path...]"
        ""
        "Options:"
        summary
        ""]
       (str/join \newline)))

(comment
  (println (print-help (:summary (parse-opts nil cli-options))))
  )

(defn print-errors
  [errors]
  (str/join \newline (cons "Ran into errors:" errors)))

(defn validate-args
  [args]
  (let [{:keys [arguments options errors summary]} (parse-opts args cli-options :in-order true)]
    (cond
      (:help options) {:exit-message (print-help summary) :ok true}
      errors {:exit-message (print-errors errors)}
      (seq arguments) {:options options
                       :paths arguments}
      :else {:exit-message (print-help summary) :ok true})))

(defn -main [& args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-args args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          (System/exit (if ok 0 1)))
      (let [ctx (atom {:violations []})
            _ (check-directories ctx paths)
            end-time (System/currentTimeMillis)
            violations (:violations @ctx)]
        (when-not (:quiet options)
          (doseq [violation (sort-by :filename violations)]
            (print-find violation)))
        (printf "Linting took %sms, %s style warnings%n"
                (int (- end-time start-time))
                (count violations))
        (flush)
        (System/exit (count violations))))))
