(ns noahtheduke.spat.rules.lint.assoc-assoc
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule assoc-assoc
  "Layering `assoc` calls are hard to read. `assoc-in` is known and idiomatic.

  Examples:

  # bad
  (assoc coll :key1 (assoc (:key2 coll) :key2 new-val))
  (assoc coll :key1 (assoc (coll :key2) :key2 new-val))
  (assoc coll :key1 (assoc (get coll :key2) :key2 new-val))

  # good
  (assoc-in coll [:key1 :key2] new-val)
  "
  {:patterns ['(assoc ?coll ?key1 (assoc (?coll ?key1) ?key2 ?val))
              '(assoc ?coll ?key1 (assoc (?key1 ?coll) ?key2 ?val))
              '(assoc ?coll ?key1 (assoc (get ?coll ?key1) ?key2 ?val))]
   :message "Use the built-in function instead of recreating it."
   :replace '(assoc-in ?coll [?key1 ?key2] ?val)})
