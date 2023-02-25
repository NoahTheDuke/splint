(ns noahtheduke.spat.rules.eq-nil
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule eq-nil
  "`nil?` exists so use it.

  Examples:

  # bad
  (= nil x)
  (= x nil)

  # good
  (nil? x)
  "
  {:patterns ['(= nil ?x)
              '(= ?x nil)]
   :message "Use the built-in function instead of recreating it."
   :replace '(nil? ?x)})
