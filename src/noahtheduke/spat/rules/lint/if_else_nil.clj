(ns noahtheduke.spat.rules.lint.if-else-nil
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-else-nil
  "Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

  Examples:

  # bad
  (if (some-func) :a nil)

  # good
  (when (some-func) :a)
  "
  {:pattern '(if ?x ?y nil)
   :message "Use `when` which doesn't require specifying the else branch."
   :replace '(when ?x ?y)})
