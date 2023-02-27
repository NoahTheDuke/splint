(ns noahtheduke.spat.rules.not-some-pred
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-some-pred
  "not-any? is succinct and meaningful.

  # bad
  (not (some even? coll))

  # good
  (not-any? even? coll)
  "
  {:pattern '(not (some ?pred ?coll))
   :message "Use the built-in function instead of recreating it."
   :replace '(not-any? ?pred ?coll)})
