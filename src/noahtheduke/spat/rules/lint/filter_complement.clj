(ns noahtheduke.spat.rules.lint.filter-complement
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule filter-complement
  "Check for (filter (complement pred) coll)

  Examples:

  # bad
  (filter (complement even?) coll)

  # good
  (remove even? coll)
  "
  {:pattern '(filter (complement ?pred) ?coll)
   :message "Use the built-in function instead of recreating it."
   :replace '(remove ?pred ?coll)})
