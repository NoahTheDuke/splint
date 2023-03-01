(ns noahtheduke.spat.rules.lint.loop-do
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule loop-do
  "`loop` has an implicit `do`. Use it.

  Examples:

  # bad
  (loop [] (do (println 1) (println 2)))

  # good
  (loop [] (println 1) (println 2))
  "
  {:pattern '(loop ?binding (do &&. ?exprs))
   :message "Unnecessary `do` in `loop` body."
   :replace '(loop ?binding &&. ?exprs)})
