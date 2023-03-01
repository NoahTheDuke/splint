(ns noahtheduke.spat.rules.lint.if-not-both
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-not-both
  "`if-not` exists, so use it.

  Examples:

  # bad
  (if (not x) y z)

  # good
  (if-not x y z)
  "
  {:pattern '(if (not ?x) ?y ?z)
   :message "Use `if-not` instead of recreating it."
   :replace '(if-not ?x ?y ?z)})
