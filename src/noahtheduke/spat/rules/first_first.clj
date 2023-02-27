(ns noahtheduke.spat.rules.first-first
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule first-first
  "ffirst is succinct and meaningful.

  Examples:

  # bad
  (first (first coll))

  # good
  (ffirst coll)
  "
  {:pattern '(first (first ?coll))
   :message "Use the built-in function instead of recreating it."
   :replace '(ffirst ?coll)})
