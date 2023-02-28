(ns noahtheduke.spat.rules.lint.nested-multiply
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule nested-multiply
  "Checks for simple nested multiply.

  Examples:

  # bad
  (* x (* y z))
  (* x (* y z a))

  # good
  (* x y z)
  (* x y z a)
  "
  {:pattern '(* ?x (* &&. ?xs))
   :message "Use the variadic arity."
   :replace '(* ?x &&. ?xs)})
