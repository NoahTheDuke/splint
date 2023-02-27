(ns noahtheduke.spat.rules.filter-seq
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule filter-seq
  "Check for (filter seq coll)

  Examples:

  # bad
  (filter seq coll)

  # good
  (remove empty? coll)
  "
  {:pattern '(filter seq ?coll)
   :message "Intent is to remove empty collections."
   :replace '(remove empty? ?coll)})
