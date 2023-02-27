(ns noahtheduke.spat.rules.let-when
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule let-when
  "`when-let` exists so use it.

  Examples:

  # bad
  (let [result (some-func)] (when result (do-stuff result)))

  # good
  (when-let [result (some-func)] (do-stuff result))
  "
  {:pattern '(let [?result ?given] (when ?result &&. ?args))
   :message "Use the built-in function instead of recreating it."
   :replace '(when-let [?result ?given] &&. ?args)})
