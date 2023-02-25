(ns noahtheduke.spat.rules.plus-one
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule plus-one
  "Checks for simple +1 that should use `clojure.core/inc`.

  Examples:

  # bad
  (+ x 1)
  (+ 1 x)

  # good
  (inc x)
  "
  {:patterns ['(+ ?x 1)
              '(+ 1 ?x)]
   :message "Use the more specific core function."
   :replace '(inc ?x)})
