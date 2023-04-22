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
  [ctx rule pattern parent-form form]
  (try
    (when-let [binds (pattern form)]
      (let [on-match (:on-match rule)]
        (on-match ctx rule (vary-meta form assoc :parent-form parent-form) binds)))
    (catch Throwable e
      (throw (ex-info (ex-message e)
                      {:form (if (seqable? form) (take 2 form) form)
                       :data (ex-data e)
                       :rule (:full-name rule)}
                      e)))))

(defn check-rule
  [ctx rule parent-form form]
  (if-let [pattern (:pattern rule)]
    (check-pattern ctx rule pattern parent-form form)
    (let [patterns (:patterns rule)]
      (reduce
        (fn [_ pattern]
          (when-let [result (check-pattern ctx rule pattern parent-form form)]
            (reduced result)))
        nil
        patterns))))

(defn check-all-rules-of-type
  [ctx rules parent-form form]
  (reduce
    (fn [acc rule-entry]
      (let [rule (val rule-entry)]
        (if (-> rule :config :enabled)
          (let [result (check-rule ctx rule parent-form form)]
            (if (some? result)
              (conj acc result)
              acc))
          acc)))
    nil
    rules))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx rules parent-form form]
  (when (seq rules)
    (when-let [diagnostics (check-all-rules-of-type ctx rules parent-form form)]
      (update ctx :diagnostics swap! into diagnostics))))

(defn update-rules [rules form]
  (if-let [disabled-rules (some-> form meta :splint/disable)]
    (if (true? disabled-rules)
      ;; disable everything
      (update-vals rules (fn [rs]
                           (update-vals rs #(assoc-in % [:config :enabled] false))))
      ;; parse list of disabled genres and specific rules
      (let [{genres true specific-rules false} (group-by simple-symbol? disabled-rules)
            genres (into #{} (map str) genres)
            specific-rules (set specific-rules)]
        (update-vals
          rules
          (fn [rs]
            (update-vals
              rs (fn [rule]
                   (let [genre (:genre rule)
                         rule-name (:full-name rule)]
                     (if (or (contains? genres genre)
                             (contains? specific-rules rule-name))
                       (assoc-in rule [:config :enabled] false)
                       rule))))))))
    rules))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx rules filename parent-form form]
  (let [form (if (meta form)
               (vary-meta form assoc :filename filename)
               form)
        rules (update-rules rules form)]
    (check-form ctx (rules (simple-type form)) parent-form form)
    (when (and (seqable? form)
               (not= 'quote (first form)))
      (run! #(check-and-recur ctx rules filename form %) form)
      nil)))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx rules ^File file]
  (when-let [parsed-file
             (try (parse-string-all (slurp file))
                  (catch Throwable e
                    (throw (ex-info (ex-message e)
                                    (assoc (ex-data e) :file file)
                                    e))))]
    (try
      ;; Check any full-file rules
      (check-form ctx (rules :file) nil parsed-file)
      (check-and-recur ctx rules (str file) nil parsed-file)
      (catch clojure.lang.ExceptionInfo e
        (throw (ex-info (ex-message e) (assoc (ex-data e) :file file) (.getCause e)))))))

(defn check-paths-parallel [ctx rules paths]
  (->> (mapcat #(file-seq (io/file %)) paths)
       (filter #(and (.isFile ^File %)
                     (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
       (pmap #(parse-and-check-file ctx rules %))
       (dorun)))

(defn check-paths-single [ctx rules paths]
  (let [xf (comp (mapcat #(file-seq (io/file %)))
                 (filter #(and (.isFile ^File %)
                               (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
                 (map #(parse-and-check-file ctx rules %)))]
    (sequence xf paths)))

(defn check-paths [ctx rules paths]
  (try
    (if (-> ctx :options :parallel)
      (check-paths-parallel ctx rules paths)
      (check-paths-single ctx rules paths))
    (catch java.util.concurrent.ExecutionException e
      (let [cause (.getCause e)
            message (str/trim (ex-message cause))
            data (ex-data cause)
            error-msg (format "Splint encountered an error in %s: %s"
                              (str (:file data)
                                   (when (:line data)
                                     (str ":" (:line data)))
                                   (when (:column data)
                                     (str ":" (:column data)))
                                   (when (:form data)
                                     (str "\nin form: " (apply list (:form data)))))
                              message)]
        (println error-msg)
        (flush))
      (System/exit 1))))

(defn prepare-rules [config rules]
  (->> config
       (reduce-kv
         (fn [rules rule-name config]
           (if (and (map? config)
                    (contains? config :enabled))
             (assoc-in rules [rule-name :config] (assoc config :rule-name rule-name))
             rules))
         rules)
       vals
       (reduce
         (fn [rules rule]
           (assoc-in rules [(:init-type rule) (:full-name rule)] rule))
         {})))

(defn prepare-context [rules config]
  (-> rules
      (assoc :diagnostics (atom []))
      (assoc :options {:help (:help config)
                       :output (:output config)
                       :parallel (:parallel config)
                       :quiet (:quiet config)})))

(defn run [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          (System/exit (if ok 0 1)))
      (let [config (load-config options)
            rules (prepare-rules config (or @global-rules {}))
            ctx (prepare-context rules config)
            _ (check-paths ctx rules paths)
            end-time (System/currentTimeMillis)
            diagnostics @(:diagnostics ctx)]
        (print-results (:options ctx) diagnostics (int (- end-time start-time)))
        (System/exit (count diagnostics))))))
