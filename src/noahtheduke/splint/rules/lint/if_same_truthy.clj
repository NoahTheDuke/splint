; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-same-truthy
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/if-same-truthy
  "`or` exists so use it lol.

  @examples

  ; avoid
  (if x x y)

  ; prefer
  (or x y)
  "
  {:pattern '(if ?x ?x ?y)
   :message "Use `or` instead of recreating it."
   :replace '(or ?x ?y)})
