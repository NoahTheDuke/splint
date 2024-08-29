; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.mapcat-apply-apply
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/mapcat-apply-apply
  "Check for `(apply concat (apply map x y))`.

  @examples

  ; avoid
  (apply concat (apply map x y))

  ; prefer
  (mapcat x y)
  "
  {:pattern '(apply concat (apply map ?x ?y))
   :message "Use `mapcat` instead of recreating it."
   :replace '(mapcat ?x ?y)})
