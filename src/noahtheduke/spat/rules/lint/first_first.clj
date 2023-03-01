(ns noahtheduke.spat.rules.lint.first-first
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
   :message "Use `ffirst` instead of recreating it."
   :replace '(ffirst ?coll)})
