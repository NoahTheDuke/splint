(ns noahtheduke.spat.rules.lint.if-not-not
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-not-not
  "Two `not`s cancel each other out.

  Examples:

  # bad
  (if-not (not x) y z)

  # good
  (if x y z)
  "
  {:pattern '(if-not (not ?x) ?y ?z)
   :message "Use `if` instead of double negation."
   :replace '(if ?x ?y ?z)})
