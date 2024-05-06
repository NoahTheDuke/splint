; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.update-in-assoc
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/update-in-assoc
  "`update-in`-ing an `assoc` with the same key are hard to read. `assoc-in` is known
  and idiomatic.

  Examples:

  ; avoid
  (update-in coll [:a :b] assoc 5)

  ; prefer
  (assoc-in coll [:a :b] 5)
  "
  {:pattern '(update-in ?coll ?keys assoc ?val)
   :message "Use `assoc-in` instead of recreating it."
   :replace '(assoc-in ?coll ?keys ?val)})
