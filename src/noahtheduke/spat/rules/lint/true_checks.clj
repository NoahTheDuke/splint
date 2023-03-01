(ns noahtheduke.spat.rules.lint.true-checks
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
   :message "Use `true?` instead of recreating it."
   :replace '(true? ?x)})
