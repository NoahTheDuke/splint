(ns noahtheduke.spat.rules.not-empty 
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-empty?
  "`seq` returns `nil` when given an empty collection. `empty?` is implemented as
  `(not (seq coll))` so it's best and fastest to use `seq` directly.

  Examples

  # bad
  (not (empty? coll))

  # good
  (seq coll)"
  {:pattern '(not (empty? ?x))
   :message "seq is idiomatic, gotta learn to love it."
   :replace '(seq ?x)})
