(ns noahtheduke.spat.rules.next-next
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
   :message "Use the built-in function instead of recreating it."
   :replace '(nnext ?coll)})
