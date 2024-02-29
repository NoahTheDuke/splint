; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.get-in-literals
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/get-in-literals
  "`clojure.core/get-in` is both polymorphic and relies on seq stepping, which has heavy overhead when the listed slots are keyword literals. Faster to call them as functions.

  Examples:

  ; bad
  (get-in m [:some-key1 :some-key2 :some-key3])

  ; good
  (-> m :some-key1 :some-key2 :some-key3)
  "
  {:pattern '(get-in m [(?+ keys keyword?)])
   :message "Use keywords as functions instead of `get-in`."
   :replace '(-> m ?keys)})
