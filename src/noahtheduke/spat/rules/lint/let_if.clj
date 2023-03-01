(ns noahtheduke.spat.rules.lint.let-if
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule let-if
  "`if-let` exists so use it. Suggestions can be wrong as there's no code-walking to
  determine if `result` binding is used in falsy branch.

  Examples:

  # bad
  (let [result (some-func)] (if result (do-stuff result) (other-stuff)))

  # good
  (if-let [result (some-func)] (do-stuff result) (other-stuff))
  "
  {:pattern '(let [?result ?given] (if ?result ?truthy ?falsy))
   :message "Use `if-let` instead of recreating it."
   :replace '(if-let [?result ?given] ?truthy ?falsy)})
