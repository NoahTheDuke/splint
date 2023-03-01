(ns noahtheduke.spat.rules.lint.when-not-not
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-not-not
  "Two `not`s cancel each other out.

  Examples:

  # bad
  (when-not (not x) y z)

  # good
  (when x y z)
  "
  {:pattern '(when-not (not ?x) &&. ?y)
   :message "Use `when` instead of double negation."
   :replace '(when ?x &&. ?y)})
