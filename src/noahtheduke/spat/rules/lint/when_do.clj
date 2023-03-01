(ns noahtheduke.spat.rules.lint.when-do
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
   :message "Unnecessary `do` in `when` body."
   :replace '(when ?x &&. ?y)})
