(ns noahtheduke.spat.rules.lint.when-not-empty
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-not-empty?
  "`seq` returns `nil` when given an empty collection. `empty?` is implemented as
  `(not (seq coll))` so it's best and fastest to use `seq` directly.

  Examples:

  # bad
  (when-not (empty? ?x) &&. ?y)

  # good
  (when (seq ?x) &&. ?y)
  "
  {:pattern '(when-not (empty? ?x) &&. ?y)
   :message "`seq` is idiomatic, gotta learn to love it."
   :replace '(when (seq ?x) &&. ?y)})
