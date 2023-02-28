(ns noahtheduke.spat.rules.lint.not-nil
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule not-nil?
  "`some?` exists so use it.

  Examples:

  # bad
  (not (nil? x))

  # good
  (some? x)
  "
  {:pattern '(not (nil? ?x))
   :message "Use the built-in function instead of recreating it."
   :replace '(some? ?x)})
