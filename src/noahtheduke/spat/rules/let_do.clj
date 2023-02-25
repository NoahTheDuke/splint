(ns noahtheduke.spat.rules.let-do 
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule let-do
  "`let` has an implicit `do`, so use it.

  # bad
  (let [a 1 b 2] (do (println a) (println b)))

  # good
  (let [a 1 b 2] (println a) (println b))
  "
  {:pattern '(let ?binding (do &&. ?exprs))
   :message "Use the built-in `do` instead of recreating it."
   :replace '(let ?binding &&. ?exprs)})
