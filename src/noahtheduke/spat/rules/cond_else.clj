(ns noahtheduke.spat.rules.cond-else 
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn not-else [form]
  (and (not= :else form)
       (or (keyword? form)
           (true? form))))

(defrule cond-else
  "It's nice when the default branch is consistent.

  Examples:

  # bad
  (cond (< 10 num) (println 10) (< 5 num) (println 5) true (println 0))

  # good
  (cond (< 10 num) (println 10) (< 5 num) (println 5) :else (println 0))
  "
  {:pattern '(cond &&. ?pairs %not-else ?else)
   :message "Use :else as the catch-all branch."
   :replace '(cond &&. ?pairs :else ?else)})
