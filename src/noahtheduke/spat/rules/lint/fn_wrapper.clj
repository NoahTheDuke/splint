(ns noahtheduke.spat.rules.lint.fn-wrapper
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn fn?? [sexp]
  (#{'fn 'fn*} sexp))

(defrule fn-wrapper
  "Avoid wrapping functions in pass-through anonymous function defitions.

  Examples:

  # bad
  (fn [num] (even? num))

  # good
  even?

  # bad
  (let [f (fn [num] (even? num))] ...)

  # good
  (let [f even?] ...)
  "
  {:patterns ['(%fn?? [?arg] (?fun ?arg))
              '(%fn?? ([?arg] (?fun ?arg)))]
   :message "No need to wrap function. Clojure supports first-class functions."
   :replace '?fun})
