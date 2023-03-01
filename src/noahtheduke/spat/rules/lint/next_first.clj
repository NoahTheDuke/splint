(ns noahtheduke.spat.rules.lint.next-first
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
   :message "Use `nfirst` instead of recreating it."
   :replace '(nfirst ?coll)})
