(ns noahtheduke.spat.rules.if-not-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-not-do
  "`if-not` already defines an implicit `do`. Rely on it.

  Examples:

  # bad
  (if-not x (do (println :a) (println :b) :c))

  # good
  (if-not x (println :a) (println :b) :c)
  "
  {:pattern '(if-not ?x (do &&. ?y))
   :message "Use the built-in `do` instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
