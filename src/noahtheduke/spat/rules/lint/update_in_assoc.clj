(ns noahtheduke.spat.rules.lint.update-in-assoc
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule update-in-assoc
  "`update-in`-ing an `assoc` with the same key are hard to read. `assoc-in` is known
  and idiomatic.

  Examples:

  # bad
  (update-in coll [:a :b] assoc 5)

  # good
  (assoc-in coll [:a :b] 5)
  "
  {:pattern '(update-in ?coll ?keys assoc ?val)
   :message "Use `assoc-in` instead of recreating it."
   :replace '(assoc-in ?coll ?keys ?val)})
