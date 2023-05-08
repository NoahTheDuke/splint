; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli
  (:require
    [clojure.data :as data]
    [clojure.pprint :as pp]
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [clojure.walk :as walk]
    [noahtheduke.splint.config :refer [default-config find-local-config load-config splint-version]]))

(def cli-options
  [["-o" "--output FMT" "Output format: simple, full, clj-kondo, markdown."
    :default "full"
    :validate [#{"simple" "full" "clj-kondo" "markdown"}
               "Not a valid output format (simple, full, clj-kondo, markdown)"]]
   ["-q" "--quiet" "Print no suggestions, only return exit code."]
   [nil "--[no-]parallel" "Run splint in parallel."
    :default true]
   [nil "--config TYPE" "Pretty-print the config. \"diff\": difference from default; \"local\": contents of loaded config file; \"full\": complete merged config."
    :validate [#{"diff" "local" "full"}
               "Not a valid selection (diff, local, full)."]]
   ["-h" "--help" "Print help information."]
   ["-v" "--version" "Print version information."]])

(defn help-message
  [summary]
  (let [lines [splint-version
               ""
               "Usage:"
               "  splint [options] [path...]"
               "  splint [options] -- [path...]"
               ""
               "Options:"
               summary
               ""]]
    {:exit-message (str/join \newline lines)
     :ok true}))

(defn pick-visible [config]
  (walk/postwalk
    (fn [obj]
      (if (and (map? obj) (contains? obj :enabled))
        (select-keys obj [:enabled :chosen-style])
        obj))
    config))

(defn print-config
  [options]
  (let [{:keys [file local]} (find-local-config)
        kind (:config options)
        result (case kind
                 "diff" (second (data/diff @default-config local))
                 "local" local
                 "full" (load-config local nil)
                 ; else
                 "Something has gone wrong")
        result (into (sorted-map)
                     (update-keys (pick-visible result) (comp symbol name)))]
    {:exit-message
     (format "%s%s:\n%s"
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
    {:options options :paths paths}))

(defn validate-opts
  "Parse and validate seq of strings.

  Returns either a map of {:exit-message \"some str\" :ok logical-boolean}
  or {:options {map of cli opts} :paths [seq of strings]}.

  :ok is false if given invalid options or an option is provided after paths."
  [args]
  (let [{:keys [arguments options errors summary]} (cli/parse-opts args cli-options :strict true)]
    (cond
      (:help options) (help-message summary)
      (:version options) {:exit-message splint-version :ok true}
      errors (print-errors errors)
      (:config options) (print-config options)
      (seq arguments) (validate-paths options arguments)
      :else (help-message summary))))
