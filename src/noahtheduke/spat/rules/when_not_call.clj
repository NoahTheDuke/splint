(ns noahtheduke.spat.rules.when-not-call
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-not-call
  "`when-not` exists so use it lol.

  Examples:

  # bad
  (when (not x) :a :b :c)

  # good
  (when-not x :a :b :c)
  "
  {:pattern '(when (not ?x) &&. ?y)
   :message "Use the built-in function instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
