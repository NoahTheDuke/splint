(ns noahtheduke.spat.rules.lint.dorun-map
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule dorun-map
  "`run!` uses `reduce` which non-lazy.

  Examples:

  # bad
  (dorun (map println (range 10)))

  # good
  (run! println (range 10))
  "
  {:pattern '(dorun (map ?fn ?coll))
   :message "Use `run!`, a non-lazy function."
   :replace '(run! ?fn ?coll)})
