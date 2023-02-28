(ns noahtheduke.spat.rules.lint.filter-fn-not-pred
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule filter-fn-not-pred
  "Avoid wrapping the predicate in `not`.

  Examples:

  # bad
  (filter #(not (even? %)) coll)
  (filter (fn [x] (not (even? x))) coll)
  (filter (comp not even?) coll)

  # good
  (remove even? coll)
  "
  {:patterns ['(filter (fn [?x] (not (?pred ?x))) ?coll)
              '(filter (fn* [?x] (not (?pred ?x))) ?coll)
              '(filter (comp not ?pred) ?coll)]
   :message "Use remove with predicate."
   :replace '(remove ?pred ?coll)})
