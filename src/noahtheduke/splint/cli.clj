; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli
  (:require
    [clojure.data :as data]
    [clojure.pprint :as pp]
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [noahtheduke.splint.clojure-ext.core :refer [postwalk*]]
    [noahtheduke.splint.config :refer [default-config find-local-config load-config splint-version]]))

(def cli-options
  [["-o" "--output FMT" "Output format: simple, full, clj-kondo, markdown, json, json-pretty, edn."
    :validate [#{"simple" "full" "clj-kondo" "markdown" "json" "json-pretty" "edn"}
               "Not a valid output format (simple, full, clj-kondo, markdown, json, json-pretty, edn)"]]
   [nil "--[no-]parallel" "Run splint in parallel. Default to true."]
   ["-q" "--quiet" "Print no diagnostics, only summary."]
   ["-s" "--silent" "Don't print suggestions or summary."]
   [nil "--errors" "Only print error diagnostics."]
   [nil "--print-config TYPE" "Pretty-print the config: diff, local, full."
    :validate [#{"diff" "local" "full"}
               "Not a valid selection (diff, local, full)."]]
   [nil "--[no-]summary" "Don't print summary. Default to true."]
   [nil "--auto-gen-config" "Generate a passing config file for chosen paths."]
   ["-h" "--help" "Print help information."]
   ["-v" "--version" "Print version information."]])

(defn help-message
  [specs]
  (let [lines [(splint-version)
               ""
               "Usage:"
               "  splint [options]"
               "  splint [options] [path...]"
               "  splint [options] -- [path...]"
               ""
               "Options:"
               (#'cli/summarize specs)
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
                     (update-keys (pick-visible result) (comp symbol name)))]
    {:exit-message
     (format "%s%s%s:\n%s"
             (if (:config options)
               "DEPRECATION WARNING: --config should be --print-config\n\n"
               "")
             (if file (format "Local config loaded from: %s\n\n" file) "")
             (str/capitalize kind)
             (with-out-str (pp/pprint result)))
     :ok true}))

(defn print-errors
  [errors]
  {:exit-message (str/join \newline (cons "splint errors:" errors))
   :ok false})

(defn validate-paths
  "Treat any path strings that begin with '--' as suspect and reject the whole call. No
  doubt this fails for some paths, but if you're doing that, get outta here."
  [options paths]
  (if-let [errors (seq (filter #(str/starts-with? % "--") paths))]
    (print-errors (mapv #(str (pr-str %) " must come before paths") errors))
    {:options options :paths (vec paths)}))

(defn validate-opts
  "Parse and validate seq of strings.

  Returns either a map of {:exit-message \"some str\" :ok logical-boolean}
  or {:options {map of cli opts} :paths [seq of strings]}.

  :ok is false if given invalid options or an option is provided after paths."
  [args]
  (let [{:keys [arguments options errors summary]}
        (cli/parse-opts args cli-options :strict true :summary-fn identity)]
    (cond
      (:help options) (help-message summary)
      (:version options) {:exit-message (splint-version) :ok true}
      errors (print-errors errors)
      (or (:config options) (:print-config options)) (print-config options)
      :else (validate-paths options arguments))))
