(ns noahtheduke.spat.rules.take-repeatedly 
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule take-repeatedly
  "`repeatedly` has an arity for limiting the number of repeats with `take`.

  # Examples

  # bad
  (take 5 (repeatedly (range 10))

  # good
  (repeatedly 5 (range 10))
  "
  {:pattern '(take ?n (repeatedly ?coll))
   :message "Rely on the arity."
   :replace '(repeatedly ?n ?coll)})
