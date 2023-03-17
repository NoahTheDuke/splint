; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [noahtheduke.spat.parser :refer [parse-string-all]]
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.cli :refer [validate-opts]]
    [noahtheduke.splint.config :refer [load-config]]
    [noahtheduke.splint.printer :refer [print-results]]
    [noahtheduke.splint.rules :refer [global-rules]])
  (:import
    (java.io File)))

(set! *warn-on-reflection* true)

(defn check-pattern
  "Call `:pattern` on the form and if it hits, call `:on-match` on it.

  Only attach `parent-form` to the metadata after `:pattern` is true, cuz
  `parent-form` can be potentially massive."
  [rule pattern parent-form form]
  (try
    (when-let [binds (pattern form)]
      (let [on-match (:on-match rule)]
        (on-match rule (vary-meta form assoc :parent-form parent-form) binds)))
    (catch Throwable e
      (throw (ex-info (ex-message e)
                      {:form (if (seqable? form) (take 2 form) form)
                       :data (ex-data e)
                       :rule (:full-name rule)}
                      e)))))

(defn check-rule
  [rule parent-form form]
  (if-let [pattern (:pattern rule)]
    (check-pattern rule pattern parent-form form)
    (let [patterns (:patterns rule)]
      (reduce
        (fn [_ pattern]
          (when-let [result (check-pattern rule pattern parent-form form)]
            (reduced result)))
        nil
        patterns))))

(defn check-all-rules-of-type
  [config rules parent-form form]
  (keep
    (fn [[rule-name rule]]
      (when (-> config rule-name :enabled)
        (check-rule rule parent-form form)))
    rules))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx config rules parent-form form]
  (when (seq rules)
    (when-let [diagnostics (check-all-rules-of-type config rules parent-form form)]
      (swap! ctx update :diagnostics into diagnostics)
      diagnostics)))

(defn update-config [config form]
  (if-let [disabled-rules (:splint/disable (meta form))]
    (if (true? disabled-rules)
      ;; disable everything
      (update-vals config (fn [v]
                            (prn v)
                            (if (and (map? v)
                                     (contains? v :enabled))
                              (assoc v :enabled false)
                              v)))
      ;; parse list of disabled genres and specific rules
      (let [{genres true specific-rules false} (group-by simple-symbol? disabled-rules)
            genres (set (map str genres))
            specific-rules (set specific-rules)]
        (->> config
             (reduce-kv
               (fn [config setting-key setting-v]
                 (if (or (contains? genres (namespace setting-key))
                         (contains? specific-rules setting-key))
                   (assoc! config setting-key (assoc setting-v :enabled false))
                   config))
               (transient config))
             (persistent!))))
    config))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx config rules filename parent-form form]
  (let [form (if (meta form)
               (vary-meta form assoc :filename filename)
               form)
        config (update-config config form)]
    (check-form ctx config (rules (simple-type form)) parent-form form)
    (when (and (seqable? form)
               (not= 'quote (first form)))
      (run! #(check-and-recur ctx config rules filename form %) form)
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
      (check-form ctx config (rules :file) nil parsed-file)
      (check-and-recur ctx config rules (str file) nil parsed-file)
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
        (print-results config diagnostics (int (- end-time start-time)))
        (System/exit (count diagnostics))))))
