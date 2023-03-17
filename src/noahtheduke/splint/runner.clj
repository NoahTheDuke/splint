; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [clojure.string :as str]
    [noahtheduke.spat.parser :refer [parse-string-all]]
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.cli :refer [validate-opts]]
    [noahtheduke.splint.config :refer [load-config]]
    [noahtheduke.splint.rules :refer [global-rules]])
  (:import
    (java.io File)))

(set! *warn-on-reflection* true)

(defn check-pattern
  [rule pattern form]
  (try
    (when-let [binds (pattern form)]
      (let [on-match (:on-match rule)]
        (on-match rule form binds)))
    (catch Throwable e
      (throw (ex-info (ex-message e)
                      {:form (if (seqable? form) (take 2 form) form)
                       :data (ex-data e)
                       :rule (:full-name rule)}
                      e)))))

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

(defn check-all-rules-of-type
  [config rules form]
  (keep
    (fn [[rule-name rule]]
      (when (-> config rule-name :enabled)
        (check-rule rule form)))
    rules))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx config rules form]
  (when (seq rules)
    (when-let [diagnostics (check-all-rules-of-type config rules form)]
      (swap! ctx update :diagnostics into diagnostics)
      diagnostics)))

(defn filter-rules [rules ignored-rules]
  (let [{genres true specific-rules false} (group-by simple-symbol? ignored-rules)
        specific-rules (set specific-rules)
        genres (set (map str genres))]
    (->> rules
         (reduce-kv
           (fn [rules rule-name rule]
             (if (or (contains? genres (:genre rule))
                     (contains? specific-rules (:full-name rule)))
               rules
               (assoc! rules rule-name rule)))
           (transient {}))
         (persistent!))))

(defn select-rules [rules form]
  (let [rules-for-type (rules (simple-type form))
        ignored-rules (:splint/disable (meta form))]
    (cond
      (nil? ignored-rules) rules-for-type
      (true? ignored-rules) nil
      (vector? ignored-rules) (filter-rules rules-for-type ignored-rules)
      :else rules-for-type)))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx config rules filename form]
  (let [form (if (meta form)
               (vary-meta form assoc :filename filename)
               form)]
    (check-form ctx config (select-rules rules form) form)
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
    (try
      ;; Check any full-file rules
      (check-form ctx config (rules :file) parsed-file)
      (check-and-recur ctx config rules (str file) parsed-file)
      (catch clojure.lang.ExceptionInfo e
        (throw (ex-info (ex-message e) (assoc (ex-data e) :file file) (.getCause e)))))))

(defn check-paths [ctx config rules paths]
  (try
    (->> (mapcat #(file-seq (io/file %)) paths)
         (filter #(and (.isFile ^File %)
                       (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
         (pmap #(parse-and-check-file ctx config rules %))
         (dorun))
    (catch java.util.concurrent.ExecutionException e
      (let [cause (.getCause e)
            message (ex-message cause)
            data (ex-data cause)]
        (printf "Splint encountered an error in %s:\n%s\nin form: %s" (:file data) message (apply list (:form data)))
        (newline)
        (flush))
      (System/exit 1))))

(defn print-find-dispatch [output _diagnostic] output)

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
  [options diagnostics]
  (when-not (:quiet options)
    (let [printer (get-method print-find (:output options))]
      (doseq [diagnostic (sort-by :filename diagnostics)]
        (printer nil diagnostic)))))

(defn run [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          (System/exit (if ok 0 1)))
      (let [ctx (atom {:diagnostics []})
            config (merge (load-config) options)
            rules (or @global-rules {})
            _ (check-paths ctx config rules paths)
            end-time (System/currentTimeMillis)
            diagnostics (:diagnostics @ctx)]
        (print-results config diagnostics)
        (printf "Linting took %sms, %s style warnings%n"
                (int (- end-time start-time))
                (count diagnostics))
        (flush)
        (System/exit (count diagnostics))))))
