(ns noahtheduke.spat.rules.lint.eq-false
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule eq-false
  "`false?` exists so use it.

  Examples:

  # bad
  (= false x)
  (= x false)

  # good
  (false? x)
  "
  {:patterns ['(= false ?x)
              '(= ?x false)]
   :message "Use `false?` instead of recreating it."
   :replace '(false? ?x)})
