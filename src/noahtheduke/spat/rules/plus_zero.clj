(ns noahtheduke.spat.rules.plus-zero
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule plus-zero
  "Checks for x + 0.

  Examples:

  # bad
  (+ x 0)
  (+ 0 x)

  # good
  x
  "
  {:patterns ['(+ ?x 0)
              '(+ 0 ?x)]
   :message "Adding 0 is a no-op."
   :replace '?x})
