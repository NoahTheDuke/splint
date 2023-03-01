(ns noahtheduke.spat.rules.lint.if-not-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  # bad
  (if-not x (do (println :a) (println :b) :c))

  # good
  (if-not x (println :a) (println :b) :c)
  "
  {:pattern '(if-not ?x (do &&. ?y))
   :message "Use `when-not` instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
