(ns noahtheduke.spat.rules.lint.minus-zero
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule minus-zero
  "Checks for x - 0.

  Examples:

  # bad
  (- x 0)

  # good
  x
  "
  {:pattern '(- ?x 0)
   :message "Subtracting 0 is a no-op."
   :replace '?x})
