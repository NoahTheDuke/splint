; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli
  (:require
   [clojure.data :as data]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [medley.core :refer [index-by]]
   [noahtheduke.splint.clojure-ext.core :refer [postwalk* update-keys*]]
   [noahtheduke.splint.config :refer [default-config find-local-config
                                      load-config splint-version]]
   [noahtheduke.splint.rules :refer [global-rules]]))

(set! *warn-on-reflection* true)

(defn kw-str->str [k]
  (let [k (name k)]
    (if (str/starts-with? k ":") (subs k 1) k)))

(def cli-options
  [["-o" "--output FMT" "Output format: simple, full, clj-kondo, markdown, json, json-pretty, edn, edn-pretty."
    :validate [(let [v #{"simple" "full" "clj-kondo" "markdown" "json" "json-pretty" "edn" "edn-pretty"}]
                 (into v (map #(str ":" %)) v))
               "Not a valid output format (simple, full, clj-kondo, markdown, json, json-pretty, edn, edn-pretty)"]
    :parse-fn kw-str->str]
   ["-r" "--require FILE" "Require additional custom rules."
    :multi true
    :update-fn (fnil conj [])]
   [nil "--only RULE" "Run only the chosen rule(s) or genre(s)."
    :multi true
    :parse-fn symbol
    :validate [#(or (contains? (:rules @global-rules) %)
                    (contains? (:genres @global-rules) %))
               "Not a valid rule."]
    :update-fn (fnil conj #{})]
   [nil "--[no-]parallel" "Run splint in parallel. Defaults to true."]
   [nil "--lang LANG" "Which dialect to check? If \"cljc\", will check both \"clj\" and \"cljs\" files/branches. Defaults to \"cljc\"."
    :validate [(let [v #{"clj" "cljs" "cljc"}]
                 (into v (map #(str ":" %)) v))
               "Not a valid dialect (clj, cljs, cljc)."]
    :parse-fn kw-str->str]
   [nil "--autocorrect" "Automatically apply safe changes."]
   [nil "--interactive" "Run autocorrect interactively."]
   ["-q" "--quiet" "Print no diagnostics, only summary."]
   ["-s" "--silent" "Don't print suggestions or summary."]
   [nil "--[no-]summary" "Don't print summary. Defaults to true."]
   [nil "--errors" "Only print error diagnostics."]
   [nil "--print-config TYPE" "Pretty-print the config: diff, local, full."
    :validate [(let [v #{"diff" "local" "full"}]
                 (into v (map #(str ":" %)) v))
               "Not a valid selection (diff, local, full)."]
    :parse-fn kw-str->str]
   [nil "--auto-gen-config" "Generate a passing config file for chosen paths."]
   ["-h" "--help" "Print help information."]
   ["-v" "--version" "Print version information."]])

(defn usage [t]
  (if (= :x t)
    ["  clojure -X:splint [key-path/value pairs]"]
    ["  clojure -M:splint [options]"
     "  clojure -M:splint [options] [path...]"
     "  clojure -M:splint [options] -- [path...]"]))

(defn example [t]
  (if (= :x t)
    "For example: clojure -X:splint :parallel false :lang :clj :output :simple :paths '[\"test/example.clj\"]'"
    "For example: clojure -M:splint --no-parallel --lang clj --output simple -- test/example.clj"))

(defn m-summary [specs]
  (cli/summarize specs))

(defn format-opt [opt]
  (as-> opt $
    (cond-> $
      (str/starts-with? $ "--") (subs 2))
    (if (str/starts-with? $ "[no-]")
      (subs $ 5)
      $)
    (str ":" $)))

(defn make-summary-part
  "Given a single compiled option spec, turn it into a formatted string."
  [spec]
  (let [{:keys [long-opt required multi desc]} spec
        opt (cond
              (and required multi)
              (str long-opt " " [(str (str/lower-case required) "...")])
              required
              (str long-opt " " (keyword (str/lower-case required)))
              :else long-opt)]
    [(format-opt opt) (or desc "")]))

(defn x-summary [specs]
  (let [parts (map make-summary-part specs)
        lens (apply map (fn [& cols] (apply max (map count cols))) parts)
        lines (cli/format-lines lens parts)]
    (str/join \newline lines)))

(defn options [t]
  (let [specs (:summary (cli/parse-opts [] cli-options :strict true :summary-fn identity))]
    (if (= :x t)
      (x-summary specs)
      (m-summary specs))))

(defn help-message [t]
  (let [lines `[~(splint-version)
                ""
                "Splint is a Clojure linter focused on style and code shape. It aims to warn about many of the guidelines in the Clojure Style Guide. It is inspired by the Ruby linter RuboCop and the Clojure linter Kibit."
                ""
                "Usage:"
                ~@(usage t)
                ""
                ~(example t)
                ""
                "Options:"
                ~(options t)
                ""]]
    {:exit-message (str/join \newline lines)
     :ok true}))

(defn pick-visible [config]
  (postwalk*
    (fn [obj]
      (if (and (map? obj) (contains? obj :enabled))
        (select-keys obj [:enabled :chosen-style])
        obj))
    config))

(defn print-config
  [options]
  (let [{:keys [file local]} (find-local-config)
        kind (:print-config options)
        result (case kind
                 "diff" (second (data/diff @default-config local))
                 "local" local
                 "full" (load-config local nil)
                 ; else
                 "Something has gone wrong")
        result (into (sorted-map)
                 (update-keys* (pick-visible result) symbol))]
    {:exit-message
     (format "%s%s%s:\n%s"
       (if (:config options)
         "DEPRECATION WARNING: --config should be --print-config\n\n"
         "")
       (if file (format "Local config loaded from: %s\n\n" file) "")
       (str/capitalize kind)
       (with-out-str (pp/pprint result)))
     :ok true}))

(defn exit-with-errors
  [errors]
  {:exit-message (str/join \newline (cons "splint errors:" errors))
   :errors errors
   :ok false})

(defn set-autocorrect-additions
  "If --interactive, set autocorrect, and filter all non-error diagnostics."
  [options]
  (let [options (if (:interactive options)
                  (assoc options :autocorrect true)
                  options)
        options (if (:autocorrect options)
                  (assoc options :errors true)
                  options)]
    options))

(def cli-opt-map
  (delay (index-by :id (#'cli/compile-option-specs cli-options))))

(defn convert-map [m]
  (let [without-paths (dissoc m :paths)
        args (for [[k v] without-paths
                   :let [cli-opt (get @cli-opt-map k)
                         o (or (:short-opt cli-opt) (:long-opt cli-opt))
                         v (if (:parse-fn cli-opt) ((:parse-fn cli-opt) v) v)]]
               (cond
                 (not (str/starts-with? o "--[no-]")) [o (str v)]
                 v [(str "--" (subs o 7))]
                 :else [(str "--no-" (subs o 7))]))]
    `[~@(into [] cat args) "--" ~@(:paths m)]))

(defn validate-opts
  "Parse and validate a map or seq of strings.

  Returns either a map of `{:exit-message \"some str\" :ok logical-boolean}`
  or `{:options {map of cli opts} :paths [seq of strings]}`.

  `:ok` is false if given invalid options or an option is provided after paths."
  [args]
  (let [t (if (map? args) :x :m)
        args' (if (= :x t) (convert-map args) args)
        {:keys [arguments options errors]} (cli/parse-opts args' cli-options :strict true :summary-fn identity)]
    (cond
      (:help options) (help-message t)
      (:version options) {:exit-message (splint-version)
                          :ok true}
      errors (exit-with-errors errors)
      (or (:config options) (:print-config options)) (print-config options)
      ; Treat any path strings that begin with '--' as suspect and reject the
      ; whole call. No doubt this fails for some paths, but if you're doing
      ; that, get outta here.
      :else (if-let [errors (seq (filter #(str/starts-with? % "--") arguments))]
              (exit-with-errors (mapv #(str (pr-str %) " must come before paths") errors))
              {:options (set-autocorrect-additions options)
               :paths (vec arguments)}))))
