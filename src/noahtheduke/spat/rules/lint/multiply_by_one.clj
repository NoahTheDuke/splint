(ns noahtheduke.spat.rules.lint.multiply-by-one
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule multiply-by-one
  "Checks for (* x 1).

  Examples:

  # bad
  (* x 1)
  (* 1 x)

  # good
  x
  "
  {:patterns ['(* ?x 1)
              '(* 1 ?x)]
   :message "Multiplying by 1 is a no-op."
   :replace '?x})
