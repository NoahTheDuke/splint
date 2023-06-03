; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [farolero.core :as faro :refer [handler-bind restart-case]]
    [noahtheduke.spat.parser :refer [parse-string-all]]
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.cli :refer [validate-opts]]
    [noahtheduke.splint.config :refer [load-config spit-config default-config]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.printer :refer [print-results]]
    [noahtheduke.splint.rules :refer [global-rules]])
  (:import
    (java.io File)
    (clojure.lang ExceptionInfo)
    (noahtheduke.splint.diagnostic Diagnostic)))

(set! *warn-on-reflection* true)

(defn exception->ex-info
  ^ExceptionInfo [^Exception ex data]
  (doto (ExceptionInfo. (or (ex-message ex) "") data ex)
    (.setStackTrace (.getStackTrace ex))))

(defn runner-error->diagnostic [^Exception e]
  (let [message (str/trim (or (ex-message e) ""))
        data (ex-data e)
        error-msg (str "Splint encountered an error: " message)]
    (->diagnostic
      {:full-name (or (:rule-name data) 'splint/error)}
      (:form data)
      {:message error-msg
       :filename (:filename data)})))

(defn check-pattern
  "Call `:pattern` on the form and if it hits, call `:on-match` on it.

  Only attach `parent-form` to the metadata after `:pattern` is true, cuz
  `parent-form` can be potentially massive.

  This has implications for pattern writing, where predicates can't rely
  on that metadata to exist."
  [ctx rule pattern parent-form form]
  (when-let [binds (pattern form)]
    (let [on-match (:on-match rule)
          diagnostics (on-match ctx rule (vary-meta form assoc :parent-form parent-form) binds)]
      (if (instance? Diagnostic diagnostics)
        diagnostics
        (doall diagnostics)))))

(defn check-rule
  "Rules either have `:pattern` or `:patterns` defined, never both.
  To avoid iteration and seq manipulation costs, handle them separately.

  Use `reduced` to early exit when checking multiple patterns as we don't
  want to create multiple diagnostics for a single form and rule."
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

(defn check-rule-ex-data [ex form rule]
  (exception->ex-info ex {:form form
                          :rule-name (:full-name rule)
                          :filename (:filename (meta form))}))

(defn check-and-accumulate
  [ctx parent-form form acc rule]
  (try
    (if (-> rule :config :enabled)
      (let [result (check-rule ctx rule parent-form form)]
        (if (some? result)
          (if (sequential? result)
            (into acc result)
            (conj acc result))
          acc))
      acc)
    (catch Exception ex
      (conj acc (runner-error->diagnostic (check-rule-ex-data ex form rule))))))

(defn check-all-rules-of-type
  "For each rule: if the rule is enabled, call `check-rule`.
  If `check-rule` returns a non-nil result, add or append it to the accumulator.
  Otherwise, return the accumulator."
  [ctx rules parent-form form]
  (reduce-kv
    (fn [acc _rule-name rule] (check-and-accumulate ctx parent-form form acc rule))
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
  (restart-case
    (try
      (when-let [parsed-file (parse-string-all file)]
        (let [parsed-file (vary-meta parsed-file assoc :filename filename)
              ctx (update ctx :checked-files swap! conj filename)]
          ;; Check any full-file rules
          (check-form ctx (rules :file) nil parsed-file)
          ;; Step over each top-level form (parent-form is nil)
          (run! #(check-and-recur ctx rules filename nil %) parsed-file)
          nil))
      (catch Exception ex
        (let [data (ex-data ex)]
          (if (= :edamame/error (:type data))
            (let [ex (ex-info (ex-message ex)
                              (assoc data
                                     :rule-name 'splint/parsing-error
                                     :filename filename
                                     :form (with-meta [] {:line (:line data)
                                                          :column (:column data)}))
                              ex)]
              (faro/error ::parse-error ex))
            (faro/error ::runner-error ex)))))
    (::faro/continue [])))

(defn check-single-file [ctx rules [file]]
  (let [[filename file]
        (cond
          (instance? java.io.File file) [(str file) (slurp file)]
          (string? file) ["example.clj" file])]
    (when filename
      (parse-and-check-file ctx rules filename file))))

(defn check-files-parallel [ctx rules files]
  (->> files
       (pmap #(parse-and-check-file ctx rules (str %) (slurp %)))
       (dorun)))

(defn check-files-serial [ctx rules files]
  (let [xf (map #(parse-and-check-file ctx rules (str %) (slurp %)))]
    (transduce xf (constantly nil) nil files)))

(defn check-files!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx rules files]
  (handler-bind [::parse-error
                 (fn [_ & [ex]]
                   (let [diagnostic (-> (runner-error->diagnostic ex)
                                        (assoc :form nil))]
                     (update ctx :diagnostics swap! conj diagnostic))
                   (faro/continue))
                 ::runner-error
                 (fn [_ & [ex]]
                   (let [diagnostic (runner-error->diagnostic ex)]
                     (update ctx :diagnostics swap! conj diagnostic))
                   (faro/continue))]
    (cond
      (-> ctx :config :dev)
      (check-single-file ctx rules files)
      (-> ctx :config :parallel)
      (check-files-parallel ctx rules files)
      :else
      (check-files-serial ctx rules files))))

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
      (assoc :config (select-keys config [:help :output :parallel :summary :quiet :silent :dev]))))

(defn resolve-files-from-paths [paths]
  (if (or (string? paths) (instance? java.io.File paths))
    [paths]
    (let [xf (comp (mapcat #(file-seq (io/file %)))
                   (filter #(and (.isFile ^File %)
                                 (some (fn [ft] (str/ends-with? % ft)) [".clj" ".cljs" ".cljc"]))))]
      (into [] xf paths))))

(defn build-result-map
  [ctx files start-time]
  (let [diagnostics @(:diagnostics ctx)
        checked-files @(:checked-files ctx)]
    {:diagnostics diagnostics
     :files (mapv str files)
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
         ctx (prepare-context rules config)
         files (resolve-files-from-paths paths)]
     (check-files! ctx rules files)
     (build-result-map ctx files start-time))))

(defn run
  "Convert command line args to usable options, pass to runner, print output."
  [args]
  (let [start-time (System/currentTimeMillis)
        {:keys [options paths exit-message ok]} (validate-opts args)]
    (cond
      exit-message
      (do (when-not (:quiet options) (println exit-message))
          {:exit (if ok 0 1)})
      (:auto-gen-config options)
      (let [all-enabled (update-vals @default-config #(assoc % :enabled true))]
        (spit-config (run-impl start-time options paths all-enabled)))
      :else
      (let [{:keys [config diagnostics total-time] :as results}
            (run-impl start-time options paths)]
        (print-results config diagnostics total-time)
        results))))

(comment
  (run ["--silent" "--no-parallel" "corpus/arglists.clj"]))
