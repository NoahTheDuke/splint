(ns noahtheduke.spat.rules.lint.conj-vector
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule conj-vector
  "`vector` is succinct and meaningful.

  Examples:

  # bad
  (conj [] :a b {:c 1})

  # good
  (vector :a b {:c 1})
  "
  {:pattern '(conj [] &&. ?x)
   :message "Use `vector` instead of recreating it."
   :replace '(vector &&. ?x)})
