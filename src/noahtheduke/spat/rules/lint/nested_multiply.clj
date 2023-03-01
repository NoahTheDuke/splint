(ns noahtheduke.spat.rules.lint.nested-multiply
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn *? [sexp]
  (#{'* '*'} sexp))

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
  {:pattern '(%*?%-?p ?x (?p &&. ?xs))
   :message "Use the variadic arity of `*`."
   :replace '(?p ?x &&. ?xs)})
