(ns noahtheduke.spat.rules.lint.minus-one
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule minus-one
  "Checks for simple -1 that should use `clojure.core/dec`.

  Examples:

  # bad
  (- x 1)

  # good
  (dec x)
  "
  {:pattern '(- ?x 1)
   :message "Use the more specific core function."
   :replace '(dec ?x)})
