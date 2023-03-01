(ns noahtheduke.spat.rules.lint.apply-str
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn not-special? [form]
  (if (list? form)
    (not (#{'reverse 'interpose} (first form)))
    true))

(defrule apply-str
  "Check for round-about clojure.string/reverse.

  Examples:

  # bad
  (apply str x)

  # good
  (clojure.string/join x)
  "
  {:pattern '(apply str %not-special?%-?coll)
   :message "Use `clojure.string/join` instead of recreating it."
   :replace '(clojure.string/join ?coll)})
