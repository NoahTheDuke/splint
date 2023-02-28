(ns noahtheduke.spat.rules.lint.divide-by-one
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule divide-by-one
  "Checks for (/ x 1).

  Examples:

  # bad
  (/ x 1)

  # good
  x
  "
  {:pattern '(/ ?x 1)
   :message "Dividing by 1 is a no-op."
   :replace '?x})
