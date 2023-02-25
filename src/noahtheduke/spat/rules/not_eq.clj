(ns noahtheduke.spat.rules.not-eq 
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
   :message "Use the built-in function instead of recreating it."
   :replace '(not= &&. ?args)})

