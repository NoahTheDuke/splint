; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [clojure.string :as str]
    [noahtheduke.splint.parser :refer [parse-string-all]]
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.cli :refer [validate-opts]]
    [noahtheduke.splint.config :refer [load-config]]
    [noahtheduke.splint.rules :refer [global-rules]])
  (:import
    (java.io File)))

(set! *warn-on-reflection* true)

(defn check-pattern
  [rule pattern form]
  (when-let [binds (pattern form)]
    (let [on-match (:on-match rule)]
      (on-match rule form binds))))

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

(defn check-rules-for-type [config rules form]
  (when-let [rules-for-type (rules (simple-type form))]
    (keep
      (fn [[rule-name rule]]
        (when (-> config rule-name :enabled)
          (check-rule rule form)))
      rules-for-type)))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  violation map and store it in `ctx`."
  [ctx config rules form]
  (when-let [violations (check-rules-for-type config rules form)]
    (swap! ctx update :violations into violations)
    violations))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx config rules filename form]
  (let [form (if (meta form)
               (vary-meta form assoc :filename filename)
               form)]
    (check-form ctx config rules form)
    (when (seqable? form)
      (run! #(check-and-recur ctx config rules filename %) form)
      nil)))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx config rules ^File file]
  (when-let [parsed-file
             (try (parse-string-all (slurp file))
                  (catch Throwable e
                    (prn (ex-info (ex-message e)
                                  (assoc (ex-data e) :filename (str file))
                                  e))))]
    (check-and-recur ctx config rules (str file) parsed-file)))

(defn check-paths [ctx config rules paths]
  (->> (mapcat #(file-seq (io/file %)) paths)
       (filter #(and (.isFile ^File %)
                     (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
       (pmap #(parse-and-check-file ctx config rules %))
       (dorun)))

(defn print-find-dispatch [output _violation] output)

(defmulti print-find #'print-find-dispatch)

(defmethod print-find "full" [_ {:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (pprint/pprint form)
  (when alt
    (println "Consider using:")
    (pprint/pprint alt))
  (newline)
  (flush))

(defmethod print-find "simple" [_ {:keys [filename rule-name line column message]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (flush))

(defmethod print-find "clj-kondo" [_ {:keys [filename line column message]}]
  (printf "%s:%s:%s: warning: %s" filename line column message)
  (newline)
  (flush))

(defn print-results
  [options violations]
  (when-not (:quiet options)
    (let [printer (get-method print-find (:output options))]
      (doseq [violation (sort-by :filename violations)]
        (printer nil violation)))))

(defn run [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          (System/exit (if ok 0 1)))
      (let [ctx (atom {:violations []})
            config (load-config)
            rules @global-rules
            _ (check-paths ctx config rules paths)
            end-time (System/currentTimeMillis)
            violations (:violations @ctx)]
        (print-results options violations)
        (printf "Linting took %sms, %s style warnings%n"
                (int (- end-time start-time))
                (count violations))
        (flush)
        (System/exit (count violations))))))
