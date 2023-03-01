(ns noahtheduke.spat.rules.lint.filter-complement
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn fn?? [sexp]
  (#{'fn 'fn*} sexp))

(defrule filter-complement
  "Check for (filter (complement pred) coll)

  Examples:

  # bad
  (filter (complement even?) coll)

  # good
  (remove even? coll)
  "
  {:patterns ['(filter (complement ?pred) ?coll)
              '(filter (%fn?? [?arg] (not (?pred ?arg))) ?coll)
              '(filter (comp not ?pred) ?coll)]
   :message "Use `remove` instead of recreating it."
   :replace '(remove ?pred ?coll)})
