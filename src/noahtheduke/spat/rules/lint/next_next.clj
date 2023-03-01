(ns noahtheduke.spat.rules.lint.next-next
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule next-next
  "nnext is succinct and meaningful.

  Examples:

  # bad
  (next (next coll))

  # good
  (nnext coll)
  "
  {:pattern '(next (next ?coll))
   :message "Use `nnext` instead of recreating it."
   :replace '(nnext ?coll)})
