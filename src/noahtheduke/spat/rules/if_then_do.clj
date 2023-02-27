(ns noahtheduke.spat.rules.if-then-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-then-do
  "Each branch of `if` can only have one expression, so using `do` to allow for multiple
  expressions is better expressed with `when`.

  Examples:

  # bad
  (if (some-func) (do (println 1) (println 2)))

  # good
  (when (some-func) (println 1) (println 2))
  "
  {:pattern '(if ?x (do &&. ?y))
   :message "Use the built-in function instead of recreating it."
   :replace '(when ?x &&. ?y)})
