(ns noahtheduke.spat.rules.when-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule when-do
  "`when` already defines an implicit `do`. Rely on it.

  Examples:

  # bad
  (when x (do (println :a) (println :b) :c))

  # good
  (when x (println :a) (println :b) :c)
  "
  {:pattern '(when ?x (do &&. ?y))
   :message "Use the built-in function instead of recreating it."
   :replace '(when ?x &&. ?y)})
