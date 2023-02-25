(ns noahtheduke.spat.rules.when-not-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  # bad
  (when-not x (do (println :a) (println :b) :c))

  # good
  (when-not x (println :a) (println :b) :c)
  "
  {:pattern '(when-not ?x (do &&. ?y))
   :message "Use the built-in function instead of recreating it."
   :replace '(when-not ?x &&. ?y)})
