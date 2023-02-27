(ns noahtheduke.spat.rules.missing-body-in-when
  (:require
    [noahtheduke.spat.rules :refer [defrule ->violation]]))

(defrule missing-body-in-when
  "`when` calls should have at least 1 expression after the condition.

  Examples:

  # bad
  (when true)
  (when (some-func))

  # good
  (when true (do-stuff))
  (when (some-func) (do-stuff))
  "
  {:pattern '(when _)
   :message "Missing body in when"
   :on-match (fn [rule form _] (->violation rule form))})
