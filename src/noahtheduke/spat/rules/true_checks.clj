(ns noahtheduke.spat.rules.true-checks
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule eq-true
  "`true?` exists so use it.

  Examples:

  # bad
  (= true x)
  (= x true)

  # good
  (true? x)
  "
  {:patterns ['(= true ?x)
              '(= ?x true)]
   :message "Use the built-in function instead of recreating it."
   :replace '(true? ?x)})
