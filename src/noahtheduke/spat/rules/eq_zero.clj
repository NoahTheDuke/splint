(ns noahtheduke.spat.rules.eq-zero
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule eq-zero
  "`zero?` exists so use it.

  Examples:

  # bad
  (= 0 num)
  (= num 0)
  (== 0 num)
  (== num 0)

  # good
  (zero? num)
  "
  {:patterns ['(= 0 ?x)
              '(= ?x 0)
              '(== 0 ?x)
              '(== ?x 0)]
   :message "Use the built-in function instead of recreating it."
   :replace '(zero? ?x)})

