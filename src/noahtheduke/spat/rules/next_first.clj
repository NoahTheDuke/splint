(ns noahtheduke.spat.rules.next-first
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule next-first
  "nfirst is succinct and meaningful.

  Examples:

  # bad
  (next (first coll))

  # good
  (nfirst coll)
  "
  {:pattern '(next (first ?coll))
   :message "Use the built-in function instead of recreating it."
   :replace '(nfirst ?coll)})
