(ns noahtheduke.spat.rules.into-literal
  (:require
    [noahtheduke.spat.rules :refer [->violation defrule]]))

(defn set-or-vec? [form]
  (and (or (set? form)
           (vector? form))
       (empty? form)))

(defrule into-literal
  "`vec` and `set` are succinct and meaningful.

  Examples:

  # bad
  (into [] coll)

  # good
  (vec coll)

  # bad
  (into #{} coll)

  # good
  (set coll)
  "
  {:pattern '(into %set-or-vec?%-?literal ?coll)
   :message "Use the built-in function instead of recreating it."
   :on-match (fn [rule form {:syms [?literal ?coll]}]
               (let [replace-form (list (if (set? ?literal) 'set 'vec) ?coll)]
                 (->violation rule form {:replace-form replace-form})))})
