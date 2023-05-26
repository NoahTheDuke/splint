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
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]])
  (:import
    (java.io File)
    (clojure.lang ExceptionInfo)))

(set! *warn-on-reflection* true)

(defn throwable->ex-info
  ^ExceptionInfo [^Throwable ex data]
  (let [new-ex (ExceptionInfo. (or (ex-message ex) "") data ex)]
    (.setStackTrace new-ex (.getStackTrace ex))
    new-ex))

(defn check-pattern
  "Call `:pattern` on the form and if it hits, call `:on-match` on it.

  Only attach `parent-form` to the metadata after `:pattern` is true, cuz
  `parent-form` can be potentially massive."
  [ctx rule pattern parent-form form]
  (try
    (when-let [binds (pattern form)]
      (let [on-match (:on-match rule)]
        (doall (on-match ctx rule (vary-meta form assoc :parent-form parent-form) binds))))
    (catch Throwable ex
      (throw (throwable->ex-info
               ex
               {:form form
                :line (:line (meta form))
                :column (:column (meta form))
                :data (ex-data ex)
                :rule (:full-name rule)})))))

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
              (if (sequential? result)
                (into acc result)
                (conj acc result))
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
  [ctx rules filename file]
  (try
    (when-let [parsed-file (parse-string-all file)]
      (let [ctx (update ctx :checked-files swap! conj filename)]
        ;; Check any full-file rules
        (check-form ctx (rules :file) nil parsed-file)
        (run! #(check-and-recur ctx rules filename nil %) parsed-file)))
    (catch Throwable e
      (throw (throwable->ex-info e (assoc (ex-data e) :file filename))))))

(defn check-paths-single [ctx rules file]
  (let [[filename file]
        (cond
          (instance? java.io.File file) [(str file) (slurp file)]
          (string? file) ["example.clj" file])]
    (when filename
      (parse-and-check-file ctx rules filename file))))

(defn check-paths-parallel [ctx rules paths]
  (->> (mapcat #(file-seq (io/file %)) paths)
       (filter #(and (.isFile ^File %)
                     (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
       (pmap #(parse-and-check-file ctx rules (str %) (slurp %)))
       (dorun)))

(defn check-paths-serial [ctx rules paths]
  (let [xf (comp (mapcat #(file-seq (io/file %)))
                 (filter #(and (.isFile ^File %)
                               (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"])))
                 (map #(parse-and-check-file ctx rules (str %) (slurp %))))]
    (transduce xf (constantly nil) nil paths)))

(defn- print-runner-error [ctx ^Throwable e]
  (let [message (str/trim (or (ex-message e) ""))
        data (ex-data e)
        error-msg (format "Splint encountered an error in %s: %s"
                          (str (:file data)
                               (when-let [line (:line data)]
                                 (str ":" line))
                               (when-let [column (:column data)]
                                 (str ":" column))
                               (when-let [form (:form data)]
                                 (str " in form " (if (seq? form)
                                                    (apply list form)
                                                    form))))
                          message)
        error (->diagnostic
                {:full-name 'splint/error}
                (:form data)
                {:message error-msg
                 :filename (str :file data)})]
    (update ctx :diagnostics swap! conj error)))

(defn check-paths!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx rules paths]
  (try
    (cond
      (-> ctx :config :dev)
      (check-paths-single ctx rules paths)
      (-> ctx :config :parallel)
      (check-paths-parallel ctx rules paths)
      :else
      (check-paths-serial ctx rules paths))
    (catch java.util.concurrent.ExecutionException e
      (print-runner-error ctx (.getCause e)))
    (catch Throwable e
      (print-runner-error ctx e))))

(defn prepare-rules [config rules]
  (->> config
       (reduce-kv
         (fn [rules rule-name config]
           (if (and (map? config)
                    (contains? config :enabled)
                    (contains? rules rule-name))
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
      (assoc :checked-files (atom []))
      (assoc :config (select-keys config [:help :output :parallel :quiet :silent :dev]))))

(defn build-result-map
  [ctx start-time]
  (let [diagnostics @(:diagnostics ctx)
        checked-files @(:checked-files ctx)]
    {:diagnostics diagnostics
     :checked-files checked-files
     :config (:config ctx)
     :total-time (int (- (System/currentTimeMillis) start-time))
     :exit (if (pos? (count diagnostics)) 1 0)}))

(defn run-impl
  "Actually perform check"
  ([start-time options paths] (run-impl start-time options paths nil))
  ([start-time options paths config]
   (let [config (or config (load-config options))
         rules (prepare-rules config (or @global-rules {}))
         ctx (prepare-context rules config)]
     (check-paths! ctx rules paths)
     (build-result-map ctx start-time))))

(defn run
  "Convert command line args to usable options, pass to runner, print output."
  [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (if exit-message
      (do (when-not (:quiet options) (println exit-message))
          {:exit (if ok 0 1)})
      (let [{:keys [config diagnostics total-time] :as results}
            (run-impl start-time options paths)]
        (print-results config diagnostics total-time)
        results))))
