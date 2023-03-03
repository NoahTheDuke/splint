; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]))

(def cli-options
  [["-h" "--help" "This message."]
   [nil "--output FORMAT" "Output format: simple, full, json"
    :default "full"
    :validate [#{"simple" "full" "json"} "Not a valid output format (simple, full, json)"]]
   ["-q" "--quiet" "Print no suggestions, only return exit code."
    :default false]])

(defn help-message
  [summary]
  (let [lines ["splint: s-expression pattern matching and linting engine"
               ""
               "Usage:"
               "  splint [options] [path...]"
               "  splint [options] -- [path...]"
               ""
               "Options:"
               summary
               ""]]
    (str/join \newline lines)))

(defn print-errors
  [errors]
  (str/join \newline (cons "splint errors:" errors)))

(defn validate-paths
  "Treat any path strings that begin with '--' as suspect and reject the whole call. No
  doubt this fails for some paths, but if you're doing that, get outta here."
  [options paths]
  (if-let [errors (seq (filter #(str/starts-with? % "--") paths))]
    {:exit-message (print-errors (mapv #(str (pr-str %) " must come before paths")
                                       errors))}
    {:options options :paths paths}))

(defn validate-opts
  "Parse and validate seq of strings.

  Returns either a map of {:exit-message \"some str\" :ok logical-boolean}
  or {:options {map of cli opts} :paths [seq of strings]}.

  :ok is false if given invalid options or an option is provided after paths."
  [args]
  (let [{:keys [arguments options errors summary]} (cli/parse-opts args cli-options :strict true)]
    (cond
      errors {:exit-message (print-errors errors)}
      (seq arguments) (validate-paths options arguments)
      :else {:exit-message (help-message summary) :ok true})))
