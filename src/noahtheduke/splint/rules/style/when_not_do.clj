; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.when-not-do
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/when-not-do
  "`when-not` already defines an implicit `do`. Rely on it.

  Examples:

  ; bad
  (when-not x (do (println :a) (println :b) :c))

  ; good
  (when-not x (println :a) (println :b) :c)
  "
  {:pattern '(when-not ?x (do ?*y) (?? _ nil?))
   :message "Unnecessary `do` in `when-not` body."
   :replace '(when-not ?x ?y)})
