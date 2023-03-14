; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.assoc-fn
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/assoc-fn
  "`assoc`-ing an update with the same key are hard to read. `update` is known and
  idiomatic.

  Examples:

  ; bad
  (assoc coll :a (+ (:a coll) 5))
  (assoc coll :a (+ (coll :a) 5))
  (assoc coll :a (+ (get coll :a) 5))

  ; good
  (update coll :a + 5)
  "
  {:patterns ['(assoc ?coll ?key (%not-assoc?%-?fn (?key ?coll) &&. ?args))
              '(assoc ?coll ?key (%not-assoc?%-?fn (?coll ?key) &&. ?args))
              '(assoc ?coll ?key (%not-assoc?%-?fn (get ?coll ?key) &&. ?args))]
   :message "Use `update` instead of recreating it."
   :replace '(update ?coll ?key ?fn &&. ?args)})
