; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.assoc-assoc
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/assoc-assoc
  "Layering `assoc` calls are hard to read. `assoc-in` is known and idiomatic.

  @examples

  ; avoid
  (assoc coll :key1 (assoc (:key2 coll) :key2 new-val))
  (assoc coll :key1 (assoc (coll :key2) :key2 new-val))
  (assoc coll :key1 (assoc (get coll :key2) :key2 new-val))

  ; prefer
  (assoc-in coll [:key1 :key2] new-val)
  "
  {:patterns ['(assoc ?coll ?key1 (assoc (?coll ?key1) ?key2 ?val))
              '(assoc ?coll ?key1 (assoc (?key1 ?coll) ?key2 ?val))
              '(assoc ?coll ?key1 (assoc (get ?coll ?key1) ?key2 ?val))]
   :message "Use `assoc-in` instead of recreating it."
   :autocorrect true
   :replace '(assoc-in ?coll [?key1 ?key2] ?val)})
