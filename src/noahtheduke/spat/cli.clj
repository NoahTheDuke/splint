(ns noahtheduke.spat.cli
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]))

(defn make-parse-opts
  "Adaption of `cli/parse-opts` which pre-compiles specs and req. Doesn't return a
  summary like `cli/parse-opts`, that can be found in [[build-summary]].

  Options can't be chosen at call-sites."
  [option-specs & {:keys [in-order no-defaults strict]}]
  (let [specs (#'cli/compile-option-specs option-specs)
        req (#'cli/required-arguments specs)]
    (fn [args]
      (let [[tokens rest-args] (#'cli/tokenize-args req args :in-order in-order)
            [opts errors] (#'cli/parse-option-tokens
                            specs tokens :no-defaults no-defaults :strict strict)]
        {:options opts
         :arguments rest-args
         :errors (when (seq errors) errors)}))))

(defn build-summary
  "Return a formatted string of the summary of the `option-specs`."
  [option-specs]
  (let [specs (#'cli/compile-option-specs option-specs)]
    (cli/summarize specs)))

(def cli-options
  [["-h" "--help" "This message"]
   [nil "--clj-kondo" "Output in clj-kondo format"
    :default false]
   ["-q" "--quiet" "Print no suggestions, only return exit code"
    :default false]])

(def ^{:arglists '([args])} parse-opts
  "Parse a sequence of arg strings."
  (make-parse-opts cli-options {:in-order true :strict true}))

(comment
  (require 'user)
  (user/bench
    (cli/parse-opts ["--quiet" "--" "src" "--clj-kondo"] cli-options
                    :in-order true :strict true))
  (user/bench
    (parse-opts ["--quiet" "src"]))
  (= (cli/parse-opts ["--quiet" "src"] cli-options :in-order true)
     ((make-parse-opts cli-options :in-order true) ["--quiet" "src"]))
  (parse-opts ["--quiet" "--" "src" "--clj-kondo"])
  ,)

(def print-help
  (let [lines ["splint: sexpr pattern matching and idiom checking"
               ""
               "Usage:"
               "  splint [options] [path...]"
               "  splint [options] -- [path...]"
               ""
               "Options:"
               (build-summary cli-options)
               ""]]
    (str/join \newline lines)))

(defn print-errors
  [errors]
  (str/join \newline (cons "Ran into errors:" errors)))

(defn validate-paths
  "Treat any path strings that begin with '--' as suspect and reject the whole call. No
  doube this fails for some paths, but if you're doing that, get outta here."
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
  (let [{:keys [arguments options errors summary]} (parse-opts args)]
    (cond
      (:help options) {:exit-message (print-help summary) :ok true}
      errors {:exit-message (print-errors errors)}
      (seq arguments) (validate-paths options arguments)
      :else {:exit-message (print-help summary) :ok true})))
