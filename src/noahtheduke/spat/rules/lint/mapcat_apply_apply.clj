(ns noahtheduke.spat.rules.lint.mapcat-apply-apply
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule mapcat-apply-apply
  "Check for (apply concat (apply map x y))

  Examples:

  # bad
  (apply concat (apply map x y))

  # good
  (mapcat x y)
  "
  {:pattern '(apply concat (apply map ?x ?y))
   :message "Use the built-in function instead of recreating it."
   :replace '(mapcat ?x ?y)})
