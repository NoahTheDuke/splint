; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns new-rule
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [selmer.parser :refer [render]]
   [clojure.java.io :as io]))

(defn make-new-rule [{n :name}]
  (let [rule-template (slurp (io/file "tasks" "new_rule.tmpl"))
        test-template (slurp (io/file "tasks" "new_rule_test.tmpl"))
        config-template (slurp (io/file "tasks" "new_rule_config.tmpl"))
        n (symbol n)
        genre (namespace n)
        rule-name (name n)]
    (assert (and genre rule-name) (format "Given %s. Gotta qualify the rule name" (pr-str n)))
    (println "Making new rule for" n)
    (let [rule-filename (io/file "src" "noahtheduke" "splint" "rules"
                          genre (namespace-munge (str rule-name ".clj")))
          test-filename (io/file "test" "noahtheduke" "splint" "rules"
                          genre (namespace-munge (str rule-name "_test.clj")))
          config-file (io/file "resources" "config" "default.edn")]
      (io/make-parents rule-filename)
      (io/make-parents test-filename)
      (spit rule-filename (render rule-template {:genre genre :rule-name rule-name}))
      (spit test-filename (render test-template {:genre genre :rule-name rule-name}))
      (-> (slurp config-file)
          (str/replace #"\{;;.*" (str/trim-newline (render config-template {:genre genre :rule-name rule-name})))
          (#(spit config-file %))))))

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
