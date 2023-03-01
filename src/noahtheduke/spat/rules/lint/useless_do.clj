(ns noahtheduke.spat.rules.lint.useless-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule useless-do
  "Examples:

  # bad
  (do coll)

  # good
  coll"
  {:pattern '(do ?x)
   :message "Unnecessary `do`."
   :replace '?x})
