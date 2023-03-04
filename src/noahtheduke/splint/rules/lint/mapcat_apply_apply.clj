; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.mapcat-apply-apply
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule mapcat-apply-apply
  "Check for (apply concat (apply map x y))

  Examples:

  ; bad
  (apply concat (apply map x y))

  ; good
  (mapcat x y)
  "
  {:pattern '(apply concat (apply map ?x ?y))
   :message "Use `mapcat` instead of recreating it."
   :replace '(mapcat ?x ?y)})
