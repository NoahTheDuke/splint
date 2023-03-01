(ns noahtheduke.spat.rules.lint.mapcat-concat-map
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule mapcat-concat-map
  "Check for (apply concat (map x y z))

  Examples:

  # bad
  (apply concat (map x y))
  (apply concat (map x y z))

  # good
  (mapcat x y)
  (mapcat x y z)
  "
  {:pattern '(apply concat (map ?x &&. ?y))
   :message "Use `mapcat` instead of recreating it."
   :replace '(mapcat ?x &&. ?y)})
