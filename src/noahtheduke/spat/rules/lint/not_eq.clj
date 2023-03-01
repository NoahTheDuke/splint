(ns noahtheduke.spat.rules.lint.not-eq
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-eq
  "`not=` exists, so use it.

  Examples:

  # bad
  (not (= num1 num2))

  # good
  (not= num1 num2)
  "
  {:pattern '(not (= &&. ?args))
   :message "Use `not=` instead of recreating it."
   :replace '(not= &&. ?args)})

