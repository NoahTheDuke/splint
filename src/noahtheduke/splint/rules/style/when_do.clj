; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.when-do
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/when-do
  "`when` already defines an implicit `do`. Rely on it.

  Examples:

  ; avoid
  (when x (do (println :a) (println :b) :c))

  ; prefer
  (when x (println :a) (println :b) :c)
  "
  {:pattern '(when ?x (do ?*y))
   :message "Unnecessary `do` in `when` body."
   :replace '(when ?x ?y)})
