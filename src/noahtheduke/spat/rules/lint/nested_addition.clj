(ns noahtheduke.spat.rules.lint.nested-addition
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule nested-addition
  "Checks for simple nested additions.

  Examples:

  # bad
  (+ x (+ y z))
  (+ x (+ y z a))

  # good
  (+ x y z)
  (+ x y z a)
  "
  {:pattern '(+ ?x (+ &&. ?xs))
   :message "Use the variadic arity."
   :replace '(+ ?x &&. ?xs)})
