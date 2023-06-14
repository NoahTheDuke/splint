; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner
  "Handles parsing and linting all of given files."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [noahtheduke.spat.parser :refer [parse-file]]
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
  (reduce
    (fn [acc rule] (check-and-accumulate ctx form acc rule))
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
          (mapv (fn [rule] (assoc-in rule [:config :enabled] false))
                rules)))
      ;; parse list of disabled genres and specific rules
      (let [{genres true specific-rules false} (group-by simple-symbol? disabled-rules)
            genres (into #{} (map str) genres)
            specific-rules (set specific-rules)]
        (update-vals
          rules-by-type
          (fn [rules]
            (mapv
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
              (run! #(check-and-recur ctx rules-by-type filename form %) form))
      ;; There is currently no need for checking MapEntry,
      ;; so check each individually.
      :map (reduce-kv
             (fn [_ k v]
               (check-and-recur ctx rules-by-type filename form k)
               (check-and-recur ctx rules-by-type filename form v)
               nil)
             nil
             form)
      (:set :vector)
      (run! #(check-and-recur ctx rules-by-type filename form %) form)
      ; else
      nil)
    nil))

(defn parse-and-check-file
  "Parse the given file and then check each form."
  [ctx rules-by-type {:keys [ext ^File file contents] :as file-obj}]
  (try
    (when-let [parsed-file (parse-file file-obj)]
      (let [ctx (-> ctx
                    (update :checked-files swap! conj file)
                    (assoc :ext ext)
                    (assoc :filename file)
                    (assoc :file-str contents))]
        ;; Check any full-file rules
        (when-let [file-rules (:file rules-by-type)]
          (check-form ctx file-rules parsed-file))
        ;; Step over each top-level form (parent-form is nil)
        (run! #(check-and-recur ctx rules-by-type file nil %) parsed-file)
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

(defn check-files-parallel [ctx rules-by-type files]
  (->> files
       (pmap #(parse-and-check-file ctx rules-by-type (slurp-file %)))
       (dorun)))

(defn check-files-serial [ctx rules-by-type ^clojure.lang.Indexed files]
  (let [cnt (count files)]
    (loop [idx (int 0)]
      (when (< idx cnt)
        (let [file (.nth files idx)]
          (parse-and-check-file ctx rules-by-type (slurp-file file))
          (recur (unchecked-inc idx)))))))

(defn check-files!
  "Call into the relevant `check-path-X` function, depending on the given config."
  [ctx rules-by-type files]
  (cond
    (-> ctx :config :dev)
    (parse-and-check-file ctx rules-by-type (first files))
    (-> ctx :config :parallel)
    (check-files-parallel ctx rules-by-type files)
    :else
    (check-files-serial ctx rules-by-type files)))

(defn prepare-rules [config rules]
  (let [conjv (fnil conj [])]
    (->> config
         (reduce-kv
           (fn [rules rule-name config]
             (if (and (map? config)
                      (contains? config :enabled)
                      (contains? rules rule-name))
               (assoc-in rules [rule-name :config] (assoc config :rule-name rule-name))
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
      (assoc :config (select-keys config [:help :output :parallel :summary :quiet :silent :dev]))))

(defn get-extension [^File file]
  (let [filename (.getName file)
        i (.lastIndexOf filename ".")]
    (when (< i (dec (count filename)))
      (subs filename (inc i)))))

(defn resolve-files-from-paths [paths]
  (if (or (string? paths) (instance? java.io.File paths))
    (let [p paths]
      (cond
        (instance? java.io.File p)
        [{:features #{:clj} :ext :clj :file p :contents (slurp p)}]
        (string? p)
        [{:features #{:clj} :ext :clj :file (io/file "example.clj") :contents p}]))
    (let [xf (comp (mapcat #(file-seq (io/file %)))
                   (distinct)
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
                                 nil)))))]
      (into [] xf paths))))

(defn build-result-map
  [ctx files start-time]
  (let [all-diagnostics @(:diagnostics ctx)
        grouped-diagnostics (group-by (juxt :filename :line :column :rule-name) all-diagnostics)
        filtered-diagnostics (mapv first (vals grouped-diagnostics))
        checked-files (into [] (distinct) @(:checked-files ctx))
        file-strs (mapv str files)
        total-time (int (- (System/currentTimeMillis) start-time))]
    {:diagnostics filtered-diagnostics
     :files file-strs
     :checked-files checked-files
     :config (:config ctx)
     :total-time total-time
     :exit (if (pos? (count filtered-diagnostics)) 1 0)}))

(defn run-impl
  "Actually perform check"
  ([start-time options paths] (run-impl start-time options paths nil))
  ([start-time options paths config]
   (let [config (or config (load-config options))
         rules-by-type (prepare-rules config (or (:rules @global-rules) {}))
         ctx (prepare-context rules-by-type config)
         files (resolve-files-from-paths paths)]
     (check-files! ctx rules-by-type files)
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
