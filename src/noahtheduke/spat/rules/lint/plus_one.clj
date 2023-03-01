(ns noahtheduke.spat.rules.lint.plus-one
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
   :message "Use `inc` instead of recreating it."
   :replace '(inc ?x)})
