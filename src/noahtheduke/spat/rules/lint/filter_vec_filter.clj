(ns noahtheduke.spat.rules.lint.filter-vec-filter
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule filter-vec-filter
  "filterv is preferable for using transients.

  Examples:

  # bad
  (vec (filter pred coll))

  # good
  (filterv pred coll)
  "
  {:pattern '(vec (filter ?pred ?coll))
   :message "Use `filterv` instead of recreating it."
   :replace '(filterv ?pred ?coll)})
