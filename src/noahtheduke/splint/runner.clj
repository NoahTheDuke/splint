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
      nil
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
  [ctx rule pattern form]
  (when-let [binds (pattern form)]
    (let [on-match (:on-match rule)
          diagnostics (on-match ctx rule form binds)]
      (if (instance? Diagnostic diagnostics)
        diagnostics
        (doall diagnostics)))))

(defn check-rule
  "Rules either have `:pattern` or `:patterns` defined, never both.
  To avoid iteration and seq manipulation costs, handle them separately.

  Use `reduced` to early exit when checking multiple patterns as we don't
  want to create multiple diagnostics for a single form and rule."
  [ctx rule form]
  (if-let [pattern (:pattern rule)]
    (check-pattern ctx rule pattern form)
    (let [patterns (:patterns rule)]
      (reduce
        (fn [_ pattern]
          (when-let [result (check-pattern ctx rule pattern form)]
            (reduced result)))
        nil
        patterns))))

(defn check-and-accumulate
  [ctx form acc rule]
  (try
    (if (-> rule :config :enabled)
      (let [result (check-rule ctx rule form)]
        (if (some? result)
          (if (sequential? result)
            (into acc result)
            (conj acc result))
          acc))
      acc)
    (catch Exception ex
      (conj acc (runner-error->diagnostic
                  (exception->ex-info ex {:form form
                                          :rule-name (:full-name rule)
                                          :filename (:filename ctx)}))))))

(defn check-all-rules-of-type
  "For each rule: if the rule is enabled, call `check-rule`.
  If `check-rule` returns a non-nil result, add or append it to the accumulator.
  Otherwise, return the accumulator."
  [ctx rules form]
  (reduce-kv
    (fn [acc _rule-name rule] (check-and-accumulate ctx form acc rule))
    nil
    rules))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx rules form]
  ;; `rules` is a map and therefore it's faster to check
  (when (pos? (count rules))
    (when-let [diagnostics (check-all-rules-of-type ctx rules form)]
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
  (let [ctx (assoc ctx :parent-form parent-form)
        rules (update-rules rules form)]
    (when-let [rules-for-type (rules (simple-type form))]
      (check-form ctx rules-for-type form))
    (when (and (seqable? form)
               (not= 'quote (first form)))
      (run! #(check-and-recur ctx rules filename form %) form)
      nil)))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx rules {:keys [ext ^File file contents]}]
  (try
      (when-let [parsed-file (parse-string-all contents ext)]
        (let [ctx (-> ctx
                      (update :checked-files swap! conj file)
                      (assoc :filename file))]
          ;; Check any full-file rules
          (check-form ctx (rules :file) parsed-file)
          ;; Step over each top-level form (parent-form is nil)
          (run! #(check-and-recur ctx rules file nil %) parsed-file)
          nil))
    (catch Exception ex
      (let [data (ex-data ex)]
        (if (= :edamame/error (:type data))
          (let [ex (ex-info (ex-message ex)
                            (assoc data
                                   :rule-name 'splint/parsing-error
                                   :filename file
                                   :form (with-meta [] {:line (:line data)
                                                        :column (:column data)}))
                            ex)
                diagnostic (-> (runner-error->diagnostic ex)
                               (assoc :form nil))]
            (update ctx :diagnostics swap! conj diagnostic))
          (let [diagnostic (runner-error->diagnostic ex)]
            (update ctx :diagnostics swap! conj diagnostic)))))))

(defn slurp-file [file-obj]
  (assoc file-obj :contents (slurp (:file file-obj))))

(defn check-files-parallel [ctx rules files]
  (->> files
       (pmap #(parse-and-check-file ctx rules (slurp-file %)))
       (dorun)))

(defn check-files-serial [ctx rules files]
  (let [xf (map #(parse-and-check-file ctx rules (slurp-file %)))]
    (transduce xf (constantly nil) nil files)))

(defn check-files!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx rules files]
  (cond
    (-> ctx :config :dev)
    (parse-and-check-file ctx rules (first files))
    (-> ctx :config :parallel)
    (check-files-parallel ctx rules files)
    :else
    (check-files-serial ctx rules files)))

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
    (let [p paths
          [file contents] (cond
                            (instance? java.io.File p) [p (slurp p)]
                            (string? p) [(io/file "example.clj") p])]
      [{:ext #{:clj} :file file :contents contents}])
    (let [xf (comp (mapcat #(file-seq (io/file %)))
                   (mapcat #(when (.isFile ^File %)
                              (cond
                                (str/ends-with? % "cljc")
                                [{:ext #{:clj :cljs} :file %}
                                 #_{:ext #{:cljs} :file %}]
                                (str/ends-with? % "clj")
                                [{:ext #{:clj} :file %}]
                                (str/ends-with? % "cljs")
                                [{:ext #{:cljs} :file %}]
                                :else nil))))]
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
         rules (prepare-rules config (or (:rules @global-rules) {}))
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
