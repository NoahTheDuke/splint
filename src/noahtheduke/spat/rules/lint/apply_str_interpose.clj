(ns noahtheduke.spat.rules.lint.apply-str-interpose
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule apply-str-interpose
  "Check for round-about str/join.

  Examples:

  # bad
  (apply str (interpose \",\" x))

  # good
  (clojure.string/join \",\" x)
  "
  {:pattern '(apply str (interpose ?x ?y))
   :message "Use `clojure.string/join` instead of recreating it."
   :replace '(clojure.string/join ?x ?y)})
