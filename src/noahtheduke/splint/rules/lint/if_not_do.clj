; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-not-do
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/if-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  ; bad
  (if-not x (do (println :a) (println :b) :c))

  ; good
  (when-not x (println :a) (println :b) :c)
  "
  {:pattern2 '(if-not ?x (do ?*y) (?? _ nil?))
   :message "Use `when-not` instead of recreating it."
   :replace '(when-not ?x ?y)})
