(ns noahtheduke.spat.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [clojure.string :as str]
    [edamame.core :as e]
    [noahtheduke.spat.cli :refer [validate-opts]]
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.spat.rules :refer [->violation global-rules]])
  (:import
    (java.io File)))

(set! *warn-on-reflection* true)

(def clj-defaults
  {; :all true
   :deref true
   :fn true
   :quote true
   :read-eval true
   :regex true
   :syntax-quote true
   :var true
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

(defn check-pattern
  [rule pattern form]
  (when-let [binds (pattern form)]
    (if-let [on-match (:on-match rule)]
      (on-match rule form binds)
      (->violation rule form {:binds binds}))))

(defn check-rule
  [rule form]
  (if-let [pattern (:pattern rule)]
    (check-pattern rule pattern form)
    (let [patterns (:patterns rule)]
      (reduce
        (fn [_ pattern]
          (when-let [result (check-pattern rule pattern form)]
            (reduced result)))
        nil
        patterns))))

(defn check-rules-for-type [given-rules form]
  (when-let [rules-for-type (given-rules (simple-type form))]
    (reduce
      (fn [_ rule]
        (when-let [violation (check-rule rule form)]
          (reduced violation)))
      nil
      (vals rules-for-type))))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  violation map and store it in `ctx`."
  [ctx rules form]
  (when-let [violation (check-rules-for-type rules form)]
    (swap! ctx update :violations conj violation)
    violation))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx rules filename form]
  (let [form (if (meta form)
               (vary-meta form assoc :filename filename)
               form)]
    (check-form ctx rules form)
    (when (seqable? form)
      (run! #(check-and-recur ctx rules filename %) form)
      nil)))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx rules ^File file]
  (when-let [parsed-file
             (try (parse-string-all (slurp file))
                  (catch Throwable e
                    (prn (ex-info (ex-message e)
                                  (assoc (ex-data e) :filename (str file))
                                  e))))]
    (check-and-recur ctx rules (str file) parsed-file)))

(defn check-paths [ctx rules paths]
  (->> (mapcat #(file-seq (io/file %)) paths)
       (filter #(and (.isFile ^File %)
                     (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
       (pmap #(parse-and-check-file ctx rules %))
       (dorun)))

(defn print-find [{:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (pprint/pprint form)
  (when alt
    (println "Consider using:")
    (pprint/pprint alt))
  (newline)
  (flush))

(defn run [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          (System/exit (if ok 0 1)))
      (let [ctx (atom {:violations []})
            rules @global-rules
            _ (check-paths ctx rules paths)
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
