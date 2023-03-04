; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.dorun-map
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule dorun-map
  "`run!` uses `reduce` which non-lazy.

  Examples:

  ; bad
  (dorun (map println (range 10)))

  ; good
  (run! println (range 10))
  "
  {:pattern '(dorun (map ?fn ?coll))
   :message "Use `run!`, a non-lazy function."
   :replace '(run! ?fn ?coll)})
