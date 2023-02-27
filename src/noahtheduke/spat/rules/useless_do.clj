(ns noahtheduke.spat.rules.useless-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule useless-do-x
  "Examples:

  # bad
  (do coll)

  # good
  coll"
  {:pattern '(do ?x)
   :message "Unnecessary wrapper."
   :replace '?x})
