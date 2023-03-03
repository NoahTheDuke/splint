(ns new-rule
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [selmer.parser :refer [render]]
    [clojure.java.io :as io]))

(defn make-new-rule [{n :name}]
  (let [template (slurp (io/file "tasks" "new_rule.tmpl"))
        n (symbol n)
        genre (namespace n)
        rule-name (name n)]
    (assert (and genre rule-name) "Gotta qualify the rule name")
    (let [filename (io/file "src" "noahtheduke" "spat" "rules"
                            genre (namespace-munge (str rule-name ".clj")))]
      (io/make-parents filename)
      (println "Making new rule for" n)
      (spit filename (render template {:genre genre :rule-name rule-name})))))

(def cli-options
  [["-h" "--help" "Choose a genre and name to make a new rule"]
   ["-n" "--name NAME" "Fully-qualified name of rule"
    :missing "Fully-qualified name must be provided"]])

(defn -main [& args]
  (let [{:keys [arguments options errors summary]} (cli/parse-opts args cli-options)
        {:keys [exit-message options]}
        (cond
          errors {:exit-message (str/join \newline (cons "New rule errors:" errors))}
          (seq arguments) {:exit-message "Please use opts"}
          (:help options) {:exit-message summary}
          :else {:options options})]
    (if exit-message
      (do (println exit-message)
          (System/exit 0))
      (make-new-rule options))))
