(ns noahtheduke.spat.rules.neg-checks
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule neg-checks
  "`neg?` exists so use it.

  Examples:

  # bad
  (< num 0)
  (> 0 num)

  # good
  (neg? x)
  "
  {:patterns ['(< ?x 0)
              '(> 0 ?x)]
   :message "Use the built-in function instead of recreating it."
   :replace '(neg? ?x)})
