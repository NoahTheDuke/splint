; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.assoc-fn
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn assoc? [sym]
  (and (symbol? sym)
    (String/.equals "assoc" (name sym))))

(defn not-assoc? [sym]
  (if (symbol? sym)
    (not (#{"assoc" "or"} (name sym)))
    true))

(defrule lint/assoc-fn
  "`assoc`-ing an update with the same key is hard to read. `update` is known and
  idiomatic.

  @examples

  ; avoid
  (assoc coll :a (+ (:a coll) 5))
  (assoc coll :a (+ (coll :a) 5))
  (assoc coll :a (+ (get coll :a) 5))

  ; prefer
  (update coll :a + 5)
  "
  {:patterns ['((? _ assoc?) ?coll ?key ((? fn not-assoc?) (?key ?coll) ?*args))
              '((? _ assoc?) ?coll ?key ((? fn not-assoc?) (?coll ?key) ?*args))
              '((? _ assoc?) ?coll ?key ((? fn not-assoc?) (get ?coll ?key) ?*args))]
   :message "Use `update` instead of recreating it."
   :autocorrect true
   :replace '(update ?coll ?key ?fn ?args)})
