; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.assoc-fn
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn not-assoc? [sym]
  (not= 'assoc sym))

(defrule lint/assoc-fn
  "`assoc`-ing an update with the same key is hard to read. `update` is known and
  idiomatic.

  Examples:

  ; bad
  (assoc coll :a (+ (:a coll) 5))
  (assoc coll :a (+ (coll :a) 5))
  (assoc coll :a (+ (get coll :a) 5))

  ; good
  (update coll :a + 5)
  "
  {:patterns2 ['(assoc ?coll ?key ((? fn not-assoc?) (?key ?coll) ?*args))
               '(assoc ?coll ?key ((? fn not-assoc?) (?coll ?key) ?*args))
               '(assoc ?coll ?key ((? fn not-assoc?) (get ?coll ?key) ?*args))]
   :message "Use `update` instead of recreating it."
   :replace '(update ?coll ?key ?fn ?*args)})
