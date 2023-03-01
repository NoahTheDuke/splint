(ns noahtheduke.spat.rules.lint.if-let-else-nil
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule if-let-else-nil
  "Idiomatic `if-let` defines both branches. `when-let` returns `nil` in the else branch.

  Examples:

  # bad
  (if-let [a 1] a nil)

  # good
  (when-let [a 1] a)
  "
  {:pattern '(if-let ?binding ?expr nil)
   :message "Use `when-let` instead of recreating it."
   :replace '(when-let ?binding ?expr)})
