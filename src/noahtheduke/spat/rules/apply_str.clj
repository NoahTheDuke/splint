(ns noahtheduke.spat.rules.apply-str
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule apply-str
  "Check for round-about clojure.string/reverse.

  Examples:

  # bad
  (apply str x)

  # good
  (clojure.string/join x)
  "
  {:pattern '(apply str ?x)
   :message "Use the built-in function instead of recreating it."
   :replace '(clojure.string/join ?x)})
