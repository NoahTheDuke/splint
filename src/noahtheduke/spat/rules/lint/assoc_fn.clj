(ns noahtheduke.spat.rules.lint.assoc-fn
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn not-assoc? [sym]
  (not= 'assoc sym))

(defrule assoc-fn
  "`assoc`-ing an update with the same key are hard to read. `update` is known and
  idiomatic.

  Examples:

  # bad
  (assoc coll :a (+ (:a coll) 5))
  (assoc coll :a (+ (coll :a) 5))
  (assoc coll :a (+ (get coll :a) 5))

  # good
  (update coll :a + 5)
  "
  {:patterns ['(assoc ?coll ?key (%not-assoc?%-?fn (?key ?coll) &&. ?args))
              '(assoc ?coll ?key (%not-assoc?%-?fn (?coll ?key) &&. ?args))
              '(assoc ?coll ?key (%not-assoc?%-?fn (get ?coll ?key) &&. ?args))]
   :message "Use `update` instead of recreating it."
   :replace '(update ?coll ?key ?fn &&. ?args)})
