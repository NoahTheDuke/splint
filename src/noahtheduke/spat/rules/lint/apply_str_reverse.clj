(ns noahtheduke.spat.rules.lint.apply-str-reverse
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule apply-str-reverse
  "Check for round-about clojure.string/reverse.

  Examples:

  # bad
  (apply str (reverse x))

  # good
  (clojure.string/reverse x)
  "
  {:pattern '(apply str (reverse ?x))
   :message "Use the built-in function instead of recreating it."
   :replace '(clojure.string/reverse ?x)})
