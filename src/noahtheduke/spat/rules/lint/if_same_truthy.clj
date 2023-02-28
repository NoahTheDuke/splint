(ns noahtheduke.spat.rules.lint.if-same-truthy
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-same-truthy
  "`or` exists so use it lol.

  Examples:

  # bad
  (if x x y)

  # good
  (or x y)
  "
  {:pattern '(if ?x ?x ?y)
   :message "Use the built-in function instead of recreating it."
   :replace '(or ?x ?y)})
