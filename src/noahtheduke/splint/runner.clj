; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [noahtheduke.splint.cli :refer [validate-opts]]
   [noahtheduke.splint.clojure-ext.core :refer [mapv* pmap* run!*]]
   [noahtheduke.splint.config :as conf]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.parser :refer [parse-file]]
   [noahtheduke.splint.path-matcher :refer [matches]]
   [noahtheduke.splint.printer :refer [print-results]]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.utils :refer [simple-type]])
  (:import
   (clojure.lang ExceptionInfo)
   (java.io File)
   (noahtheduke.splint.diagnostic Diagnostic)))

(set! *warn-on-reflection* true)

(defn exception->ex-info
  ^ExceptionInfo [^Exception ex data]
  (doto (ExceptionInfo. (or (ex-message ex) "") data ex)
    (.setStackTrace (.getStackTrace ex))))

(defn runner-error->diagnostic [^Exception ex]
  (let [data (ex-data ex)
        message (str/trim (or (:message data)
                            (ex-message ex)
                            ""))
        error-msg (format "Splint encountered an error%s: %s"
                    (if-let [rule-name (:rule-name data)]
                      (str " during '" rule-name)
                      "")
                    message)]
    (->diagnostic
      nil
      {:full-name (or (:error-name data) 'splint/error)}
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

(defn check-all-rules-of-type
  "For each rule: if the rule is enabled, call `check-rule`.
  If `check-rule` returns a non-nil result, add or append it to the accumulator.
  Otherwise, return the accumulator."
  [ctx rules form]
  (reduce
    (fn [acc rule]
      (try
        (if (-> rule :config :enabled)
          (let [result (check-rule ctx rule form)]
            (if (nil? result)
              acc
              (if (sequential? result)
                (into acc result)
                (conj acc result))))
          acc)
        (catch Exception ex
          (conj acc (runner-error->diagnostic
                      (exception->ex-info ex {:form form
                                              :rule-name (:full-name rule)
                                              :message (ex-message ex)
                                              :filename (:filename ctx)}))))))
    nil
    rules))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx rules form]
  ;; `rules` is a vector and therefore it's faster to check
  (when (pos? (count rules))
    (when-let [diagnostics (check-all-rules-of-type ctx rules form)]
      (update ctx :diagnostics swap! into diagnostics))))

(defn update-rules [rules-by-type form]
  (if-let [disabled-rules (some-> form meta :splint/disable)]
    (if (true? disabled-rules)
      ;; disable everything
      (update-vals
        rules-by-type
        (fn [rules]
          (mapv* (fn [rule]
                   (assoc-in rule [:config :enabled] false))
            rules)))
      ;; parse list of disabled genres and specific rules
      (let [{genres true specific-rules false} (group-by simple-symbol? disabled-rules)
            genres (into #{} (map str) genres)
            specific-rules (set specific-rules)]
        (update-vals
          rules-by-type
          (fn [rules]
            (mapv*
              (fn [rule]
                (let [genre (:genre rule)
                      rule-name (:full-name rule)]
                  (if (or (contains? genres genre)
                        (contains? specific-rules rule-name))
                    (assoc-in rule [:config :enabled] false)
                    rule)))
              rules)))))
    rules-by-type))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx rules-by-type filename parent-form form]
  (let [ctx (assoc ctx :parent-form parent-form)
        rules-by-type (update-rules rules-by-type form)
        form-type (simple-type form)]
    (when-let [rules-for-type (rules-by-type form-type)]
      (check-form ctx rules-for-type form))
    ;; Can't recur in non-seqable forms
    (case form-type
      :list (when-not (= 'quote (first form))
              (run!* #(check-and-recur ctx rules-by-type filename form %) form))
      ;; There is currently no need for checking MapEntry,
      ;; so check each individually.
      :map (run!*
             (fn [kv]
               (check-and-recur ctx rules-by-type filename form (key kv))
               (check-and-recur ctx rules-by-type filename form (val kv)))
             form)
      (:set :vector)
      (run!* #(check-and-recur ctx rules-by-type filename form %) form)
      ; else
      nil)
    nil))

(defn right-ext? [ext rule]
  (if (:ext rule)
    (when (contains? (:ext rule) ext)
      rule)
    rule))

(defn right-path? [ctx rule]
  (when rule
    (if-let [excludes (some-> rule :config :excludes)]
      (when (not-any? #(matches % (:filename ctx)) excludes)
        rule)
      rule)))

(defn pre-filter-rules
  "Fully remove disabled rules or rules that don't apply to the current filetype."
  [ctx rules-by-type]
  (let [ext (:ext ctx)]
    (update-vals
      rules-by-type
      (fn [rules]
        (into []
          (keep (fn [rule]
                  (some->> rule
                    (right-ext? ext)
                    (right-path? ctx))))
          rules)))))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx rules-by-type {:keys [ext ^File file contents] :as file-obj}]
  (try
    (when-let [parsed-file (parse-file file-obj)]
      (let [ctx (-> ctx
                  (update :checked-files swap! conj file)
                  (assoc :ext ext)
                  (assoc :filename file)
                  (assoc :file-str contents))
            rules-by-type (pre-filter-rules ctx rules-by-type)]
        ;; Check any full-file rules
        (when-let [file-rules (:file rules-by-type)]
          (check-form ctx file-rules parsed-file))
        ;; Step over each top-level form (parent-form is nil)
        (run!* #(check-and-recur ctx rules-by-type file nil %) parsed-file)
        nil))
    (catch Exception ex
      (let [data (ex-data ex)]
        (if (= :edamame/error (:type data))
          (let [data (-> data
                       (assoc :error-name 'splint/parsing-error)
                       (assoc :filename file)
                       (assoc :form (with-meta [] {:line (:line data)
                                                   :column (:column data)})))
                ex (exception->ex-info ex data)
                diagnostic (-> (runner-error->diagnostic ex)
                             (assoc :form nil))]
            (update ctx :diagnostics swap! conj diagnostic))
          (let [ex (exception->ex-info ex {:error-name 'splint/unknown-error
                                           :filename file})
                diagnostic (runner-error->diagnostic ex)]
            (update ctx :diagnostics swap! conj diagnostic)))))))

(defn slurp-file [file-obj]
  (if (:contents file-obj)
    file-obj
    (assoc file-obj :contents (slurp (:file file-obj)))))

(defn check-files-parallel [ctx rules-by-type files]
  (pmap* #(parse-and-check-file ctx rules-by-type (slurp-file %)) files))

(defn check-files-serial [ctx rules-by-type files]
  (mapv* #(parse-and-check-file ctx rules-by-type (slurp-file %)) files))

(defn check-files!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx rules-by-type files]
  (cond
    (-> ctx :config :parallel)
    (check-files-parallel ctx rules-by-type files)
    :else
    (check-files-serial ctx rules-by-type files)))

(defn require-files! [config]
  (doseq [f (:required-files config)]
    (try (load-file f)
      (catch java.io.FileNotFoundException _
        (println "Can't load" f "as it doesn't exist.")))))

(defn support-clojure-version?
  [config rule]
  (if-let [{:keys [major minor incremental]} (:min-clojure-version rule)]
    (let [current-version (:clojure-version config)]
      (and (if major
             (<= major (:major current-version))
             true)
        (if minor
          (<= minor (:minor current-version))
          true)
        (if incremental
          (<= incremental (:incremental current-version))
          true)))
    true))

(defn prepare-rules [config rules]
  (let [conjv (fnil conj [])]
    (->> config
      (reduce-kv
        (fn [rules rule-name rule-config]
          (if (and (map? rule-config)
                (contains? rule-config :enabled))
            (if-let [rule (rules rule-name)]
              (let [rule-config (assoc rule-config :rule-name rule-name)
                    rule-config (if (support-clojure-version? config rule)
                                  rule-config
                                  (assoc rule-config :enabled false))]
                (assoc-in rules [rule-name :config] rule-config))
              rules)
            rules))
        rules)
      (vals)
      (sort-by :full-name)
      (reduce
        (fn [rules rule]
          (update rules (:init-type rule) conjv rule))
        {}))))

(defn prepare-context [rules config]
  (-> rules
    (assoc :diagnostics (atom []))
    (assoc :checked-files (atom []))
    (assoc :config config)))

(defn get-extension [^File file]
  (let [filename (.getName file)
        i (.lastIndexOf filename ".")]
    (when (< i (dec (count filename)))
      (subs filename (inc i)))))

(defn- make-path-obj [path]
  (cond
    (map? path)
    (mapcat (fn [^File file]
              (when (.isFile file)
                (case (get-extension file)
                  "cljc"
                  [{:features #{:clj} :ext :cljc :file file}
                   {:features #{:cljs} :ext :cljc :file file}]
                  "clj"
                  [{:features #{:clj} :ext :clj :file file}]
                  "cljs"
                  [{:features #{:cljs} :ext :cljs :file file}]
                  ; else
                  nil)))
      (file-seq (io/file (:path path))))
    (string? path)
    [{:file (io/file "example.clj")
      :contents path
      :features #{:clj}
      :ext :clj}]
    (instance? java.io.File path)
    [{:file path
      :contents (slurp path)
      :features #{:clj}
      :ext :clj}]))

(defn resolve-files-from-paths [ctx paths]
  (let [excludes (-> ctx :config :global :excludes)
        xf (comp (mapcat make-path-obj)
             (distinct)
             (filter (fn [file-obj]
                       (if excludes
                         (not-any? #(matches % (:file file-obj)) excludes)
                         true))))]
    (into [] xf paths)))

(defn build-result-map
  [ctx files]
  (let [all-diagnostics @(:diagnostics ctx)
        grouped-diagnostics (group-by (juxt :filename :line :column :rule-name) all-diagnostics)
        filtered-diagnostics (mapv* (comp first val) grouped-diagnostics)
        checked-files (into [] (distinct) @(:checked-files ctx))
        file-strs (mapv* str files)]
    {:diagnostics filtered-diagnostics
     :files file-strs
     :checked-files checked-files
     :config (:config ctx)
     :exit (if (pos? (count filtered-diagnostics)) 1 0)}))

(defn run-impl
  "Actually perform check."
  ([paths options] (run-impl paths options nil))
  ([paths options rules]
   (require-files! options)
   (let [config (or (:test-config options) (conf/load-config options))
         rules-by-type (prepare-rules config (or rules (:rules @global-rules)))
         ctx (prepare-context rules-by-type config)
         files (resolve-files-from-paths ctx paths)]
     (check-files! ctx rules-by-type files)
     (build-result-map ctx files))))

(defn run
  "Convert command line args to usable options, pass to runner, print output."
  [args]
  (try
    (let [start-time (System/currentTimeMillis)
          {:keys [options paths exit-message ok]} (validate-opts args)
          project-file (conf/read-project-file
                         (io/file "deps.edn") (io/file "project.clj"))
          paths (mapv* #(hash-map :path %)
                  (or (not-empty paths) (:paths project-file)))
          options (assoc options :clojure-version (or (:clojure-version project-file)
                                                    *clojure-version*))]
      (cond
        exit-message
        (do (when-not (:quiet options) (println exit-message))
          {:exit (if ok 0 1)})
        (empty? paths)
        (do (when-not (:quiet options)
              (println "splint errors:")
              (println "Paths must be provided in a project file (project.clj or deps.edn) or as the final arguments when calling. See --help for details."))
          {:exit 1})
        (:auto-gen-config options)
        (let [all-enabled (update-vals @conf/default-config #(assoc % :enabled true))]
          (conf/spit-config (run-impl paths options all-enabled)))
        :else
        (let [results (run-impl paths options)
              total-time (int (- (System/currentTimeMillis) start-time))
              results (assoc results :total-time total-time)]
          (print-results results)
          results)))
    (catch Exception ex
      (let [data (ex-data ex)]
        (case (:type data)
          :config (do (println "Error reading" (str (:file data)))
                    (println (ex-message ex))
                    {:exit 1})
          ; else
          (throw ex))))))

(comment
  (dotimes [_ 100]
    (run ["--silent" "--no-parallel"]))
  (prn :heck)
  (do (require '[clj-async-profiler.core :as prof])
    (prof/profile
      (run ["--silent" "--no-parallel"]))
    nil))
