; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli
  (:require
   [clojure.data :as data]
   [clojure.pprint :as pp]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [noahtheduke.splint.clojure-ext.core :refer [postwalk* update-keys*]]
   [noahtheduke.splint.config :refer [default-config find-local-config
                                      load-config splint-version]]
   [noahtheduke.splint.rules :refer [global-rules]]))

(set! *warn-on-reflection* true)


(s/def ::output
  (let [o #{"simple" "full" "clj-kondo" "markdown" "json" "json-pretty" "edn" "edn-pretty"}]
    (into o (map keyword) o)))
(s/def ::require (s/* string?))
(s/def ::only-entry (s/and symbol?
                #(or (contains? (:rules @global-rules) %)
                   (contains? (:genres @global-rules) %))))
(s/def ::only (s/coll-of ::only-entry :into #{}))
(s/def ::parallel (s/nilable boolean?))
(s/def ::lang #{"clj" "cljs" "cljc"})
(s/def ::autocorrect (s/nilable boolean?))
(s/def ::interactive (s/nilable boolean?))
(s/def ::quiet (s/nilable boolean?))
(s/def ::silent (s/nilable boolean?))
(s/def ::summary (s/nilable boolean?))
(s/def ::errors (s/nilable boolean?))
(s/def ::print-config #{"diff" "local" "full"})
(s/def ::auto-gen-config (s/nilable boolean?))
(s/def ::help (s/nilable boolean?))
(s/def ::version (s/nilable boolean?))

(s/def ::paths (s/coll-of string?))
(s/def ::options (s/keys :opt-un [::output ::require ::only ::parallel ::lang ::autocorrect ::interactive ::quiet ::silent ::summary ::errors ::print-config ::auto-gen-config ::help ::version ::paths]))

(def cli-options
  [["-o" "--output FMT" "Output format: simple, full, clj-kondo, markdown, json, json-pretty, edn, edn-pretty."
    :validate [#(s/valid? ::output %)
               "Not a valid output format (simple, full, clj-kondo, markdown, json, json-pretty, edn, edn-pretty)"]]
   ["-r" "--require FILE" "Require additional custom rules."
    :multi true
    :update-fn (fnil conj [])]
   [nil "--only RULE" "Run only the chosen rule(s) or genre(s)."
    :multi true
    :parse-fn symbol
    :validate [#(s/valid? ::only-entry %)
               "Not a valid rule."]
    :update-fn (fnil conj #{})]
   [nil "--[no-]parallel" "Run splint in parallel. Defaults to true."]
   [nil "--lang LANG" "Which dialect to check? Used to get branches in reader conditionals as well. If \"cljc\", will check both \"clj\" and \"cljs\" files/branches. Defaults to \"cljc\"."
    :validate [#(s/valid? ::lang %)
               "Not a valid dialect (clj, cljs, cljc)."]]
   [nil "--autocorrect" "Automatically apply safe changes."]
   [nil "--interactive" "Run autocorrect interactively."]
   ["-q" "--quiet" "Print no diagnostics, only summary."]
   ["-s" "--silent" "Don't print suggestions or summary."]
   [nil "--[no-]summary" "Don't print summary. Defaults to true."]
   [nil "--errors" "Only print error diagnostics."]
   [nil "--print-config TYPE" "Pretty-print the config: diff, local, full."
    :validate [#(s/valid? ::print-config %)
               "Not a valid selection (diff, local, full)."]]
   [nil "--auto-gen-config" "Generate a passing config file for chosen paths."]
   ["-h" "--help" "Print help information."]
   ["-v" "--version" "Print version information."]])

(defn help-message []
  (let [specs (cli/parse-opts [] cli-options :strict true :summary-fn identity)
        lines [(splint-version)
               ""
               "Usage:"
               "  splint [options]"
               "  splint [options] [path...]"
               "  splint [options] -- [path...]"
               ""
               "Options:"
               (cli/summarize specs)
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

(defn validate-map-opts
  [opts]
  (let [parsed (s/conform ::options opts)]
    (if (s/invalid? parsed)
      {:errors [(with-out-str (s/explain ::options opts))]
       :options (dissoc opts :paths)
       :arguments (:paths opts)}
      (let [options (cond-> parsed
                      (:paths parsed) (dissoc :paths)
                      (:output parsed) (update :output name)
                      (:lang parsed) (update :lang keyword)
                      (:print-config parsed) (update :print-config keyword))]
        {:options options
         :arguments (or (:paths parsed) [])}))))

(defn validate-opts
  "Parse and validate a map or seq of strings.

  Returns either a map of `{:exit-message \"some str\" :ok logical-boolean}`
  or `{:options {map of cli opts} :paths [seq of strings]}`.

  `:ok` is false if given invalid options or an option is provided after paths."
  [args]
  (let [{:keys [arguments options errors]}
        (if (map? args)
          (validate-map-opts args)
          (cli/parse-opts args cli-options :strict true :summary-fn identity))]
    (cond
      (:help options) (help-message)
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
