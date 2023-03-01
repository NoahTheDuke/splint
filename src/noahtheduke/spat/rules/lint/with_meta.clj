(ns noahtheduke.spat.rules.lint.with-meta
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule with-meta-f-meta
  "`vary-meta` works like swap!, so no need to access and overwrite in two steps.

  Examples:

  # bad
  (with-meta x (assoc (meta x) :filename filename))

  # good
  (vary-meta x assoc :filename filename)
  "
  {:pattern '(with-meta ?x (?f (meta ?x) &&. ?args))
   :message "Use `vary-meta` instead of recreating it."
   :replace '(vary-meta ?x ?f &&. ?args)})
