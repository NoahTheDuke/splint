(ns noahtheduke.spat.rules.loop-do 
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
   :message "Use the built-in `do` instead of recreating it."
   :replace '(loop ?binding &&. ?exprs)})
