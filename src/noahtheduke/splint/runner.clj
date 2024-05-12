; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
   [clojure.java.io :as io]
   [noahtheduke.splint.cli :refer [validate-opts]]
   [noahtheduke.splint.clojure-ext.core :refer [mapv* pmap* run!*]]
   [noahtheduke.splint.config :as conf]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.parser :refer [parse-file]]
   [noahtheduke.splint.path-matcher :refer [matches]]
   [noahtheduke.splint.printer :refer [print-results]]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.utils :refer [simple-type support-clojure-version?]])
  (:import
   (java.io File)
   (noahtheduke.splint.diagnostic Diagnostic)))

(set! *warn-on-reflection* true)

(defn runner-error->diagnostic [ex data]
  (let [rule-name (cond-> ""
                    (:rule-name data) (str " during '" (:rule-name data))
                    true (format " (%s)" (.getName (class ex))))
        message (or (ex-message ex) "")
        error-msg (format "Splint encountered an error%s: %s"
                          rule-name
                          message)]
    (->diagnostic
      nil
      {:full-name (:error-name data)}
      (:form data)
      (-> data
          (assoc :message error-msg)
          (assoc :exception (Throwable->map ex))))))

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
  [ctx rule-names form]
  (reduce
    (fn [acc rule-name]
      (let [rule (-> ctx :rules rule-name)]
        (if (-> rule :config :enabled)
          (try
            (let [result (check-rule ctx rule form)]
              (if (nil? result)
                acc
                (if (sequential? result)
                  (into acc result)
                  (conj acc result))))
            (catch Exception ex
              (conj acc (runner-error->diagnostic
                          ex {:error-name 'splint/error
                              :form form
                              :rule-name (:full-name rule)
                              :filename (:filename ctx)}))))
          acc)))
    nil
    rule-names))

(defn check-form
  "Checks a given form against the appropriate rules then calls `on-match` to build the
  diagnostic and store it in `ctx`."
  [ctx rule-names form]
  (when-let [diagnostics (check-all-rules-of-type ctx rule-names form)]
    (update ctx :diagnostics swap! into diagnostics)))

(defn update-rules [rules-map form]
  (if-let [disabled-rules (some-> form meta :splint/disable)]
    (if (true? disabled-rules)
      ;; disable everything
      (update-vals
        rules-map
        (fn [rule]
          (assoc-in rule [:config :enabled] false)))
      ;; parse list of disabled genres and specific rules
      (let [{genres true specific-rules false} (group-by simple-symbol? disabled-rules)
            genres (into #{} (map str) genres)
            specific-rules (set specific-rules)]
        (update-vals
          rules-map
          (fn [rule]
            (let [genre (:genre rule)
                  rule-name (:full-name rule)]
              (if (or (contains? genres genre)
                    (contains? specific-rules rule-name))
                (assoc-in rule [:config :enabled] false)
                rule))))))
    rules-map))

(defn check-and-recur
  "Check a given form and then map recur over each of the form's children."
  [ctx filename parent-form form]
  (let [ctx (-> ctx
              (assoc :parent-form parent-form)
              (update :rules update-rules form))
        form-type (simple-type form)]
    (when-let [rules-for-type (-> ctx :rules-by-type form-type not-empty)]
      (check-form ctx rules-for-type form))
    ;; Can't recur in non-seqable forms
    (case form-type
      :list (when-not (= 'quote (first form))
              (run!* #(check-and-recur ctx filename form %) form))
      ;; There is currently no need for checking MapEntry,
      ;; so check each individually.
      :map (run!*
             (fn [kv]
               (check-and-recur ctx filename form (key kv))
               (check-and-recur ctx filename form (val kv)))
             form)
      (:set :vector)
      (run!* #(check-and-recur ctx filename form %) form)
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
  [ctx]
  (let [ext (:ext ctx)]
    (update
      ctx
      :rules
      update-vals
      (fn [rule]
        (if (map? rule)
          (some->> rule
            (right-ext? ext)
            (right-path? ctx))
          rule)))))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx {:keys [ext ^File file contents] :as file-obj}]
  (try
    (when-let [parsed-file (parse-file file-obj)]
      (let [ctx (-> ctx
                  (update :checked-files swap! conj file)
                  (assoc :ext ext)
                  (assoc :filename file)
                  (assoc :file-str contents)
                  (pre-filter-rules))]
        ;; Check any full-file rules
        (when-let [file-rules (-> ctx :rules-by-type :file not-empty)]
          (check-form ctx file-rules parsed-file))
        ;; Step over each top-level form (parent-form is nil)
        (run!* #(check-and-recur ctx file nil %) parsed-file)
        nil))
    (catch Exception ex
      (let [data (ex-data ex)]
        (if (= :edamame/error (:type data))
          (let [data (-> data
                       (assoc :error-name 'splint/parsing-error)
                       (assoc :filename file)
                       (assoc :form-meta {:line (:line data)
                                          :column (:column data)}))
                diagnostic (runner-error->diagnostic ex data)]
            (update ctx :diagnostics swap! conj diagnostic))
          (let [diagnostic (runner-error->diagnostic
                             ex {:error-name 'splint/unknown-error
                                 :filename file})]
            (update ctx :diagnostics swap! conj diagnostic)))))))

(defn slurp-file [file-obj]
  (if (:contents file-obj)
    file-obj
    (assoc file-obj :contents (slurp (:file file-obj)))))

(defn check-files-parallel [ctx files]
  (pmap* #(parse-and-check-file ctx (slurp-file %)) files))

(defn check-files-serial [ctx files]
  (mapv* #(parse-and-check-file ctx (slurp-file %)) files))

(defn check-files!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx files]
  (cond
    (-> ctx :config :parallel)
    (check-files-parallel ctx files)
    :else
    (check-files-serial ctx files)))

(defn require-files! [config]
  (doseq [f (:required-files config)]
    (try (load-file f)
      (catch java.io.FileNotFoundException _
        (println "Can't load" f "as it doesn't exist.")))))

(defn prepare-rules [config rules]
  (let [rule-names (set (concat (keys config) (keys rules)))]
    (-> (reduce
          (fn [acc rule-name]
            (if (symbol? rule-name)
              (let [rule (rules rule-name)
                    rule-config (config rule-name)
                    rule (if (and (map? rule-config)
                               (contains? rule-config :enabled))
                           (let [rule-config (assoc rule-config :rule-name rule-name)
                                 rule-config (if (support-clojure-version?
                                                   (:min-clojure-version rule)
                                                   (:clojure-version config))
                                               rule-config
                                               (assoc rule-config :enabled false))]
                             (assoc rule :config rule-config))
                           rule)]
                (-> acc
                  (assoc-in [:rules rule-name] rule)
                  (update-in [:rules-by-type (:init-type rule)] conj rule-name)))
              acc))
          {:rules {}
           :rules-by-type {}}
          rule-names)
      (update :rules-by-type update-vals sort))))

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
  [paths options]
  (require-files! options)
  (let [config (or (:config-override options) (conf/load-config options))
        rules-by-type (prepare-rules config (:rules @global-rules))
        config (apply dissoc config (keys (:rules rules-by-type)))
        ctx (prepare-context rules-by-type config)
        files (resolve-files-from-paths ctx paths)]
    (check-files! ctx files)
    (build-result-map ctx files)))

(defn auto-gen-config [paths options]
  (let [all-enabled (update-vals @conf/default-config #(assoc % :enabled true))]
    (conf/spit-config (run-impl paths {:config-override (merge options all-enabled)}))))

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
        (auto-gen-config paths options)
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
  (do (require '[clj-async-profiler.core :as prof])
    (prof/profile
      (run ["--silent" "--no-parallel"]))
    nil))
