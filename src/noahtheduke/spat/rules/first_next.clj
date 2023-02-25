(ns noahtheduke.spat.rules.first-next
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule first-next
  "fnext is succinct and meaningful.

  Examples:

  # bad
  (first (next coll))

  # good
  (fnext coll)
  "
  {:pattern '(first (next ?coll))
   :message "Use the built-in function instead of recreating it."
   :replace '(fnext ?coll)})
