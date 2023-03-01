(ns noahtheduke.spat.rules.lint.pos-checks
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule pos-checks
  "`pos?` exists so use it.

  Examples:

  # bad
  (< 0 num)
  (> num 0)

  # good
  (pos? x)
  "
  {:patterns ['(< 0 ?x)
              '(> ?x 0)]
   :message "Use `pos?` instead of recreating it."
   :replace '(pos? ?x)})
