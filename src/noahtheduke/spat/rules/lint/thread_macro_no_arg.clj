(ns noahtheduke.spat.rules.lint.thread-macro-no-arg
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn thread-macro? [node]
  (#{'-> '->>} node))

(defrule thread-macro-no-arg
  "Avoid wrapping vars in a threading macro.

  Examples:

  # bad
  (-> x)
  (->> x)

  # good
  x
  "
  {:pattern '(%thread-macro? ?x)
   :message "Single-arg threading macros are a no-op."
   :replace '?x})
