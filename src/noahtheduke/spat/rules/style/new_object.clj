(ns noahtheduke.spat.rules.style.new-object 
  (:require
    [noahtheduke.spat.rules :refer [defrule ->violation]]))

(defrule new-object
  "`new` is discouraged for dot usage.

  Examples:

  # bad
  (new java.util.ArrayList 100)

  # good
  (java.util.ArrayList. 100)
  "
  {:pattern '(new ?class &&. ?args)
   :message "dot creation is preferred."
   :on-match (fn [rule form {:syms [?class ?args]}]
               (let [class-dot (symbol (str ?class "."))
                     new-form `(~class-dot ~@?args)]
                 (->violation rule form {:replace-form new-form})))})
