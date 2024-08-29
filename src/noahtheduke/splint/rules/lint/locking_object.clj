; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.locking-object
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.utils :refer [simple-type]]))

(set! *warn-on-reflection* true)

(defn not-symbol? [obj]
  (not (symbol? obj)))

(defrule lint/locking-object
  "Synchronizing on interned objects is really bad. If multiple places lock on the same type of interned objects, those places are competing for locks.

  @examples

  ; avoid
  (locking :hello (+ 1 1))

  ; prefer
  (def hello (Object.))
  (locking hello (+ 1 1))
  "
  {:pattern '(locking (? obj not-symbol?) (?* _))
   :on-match (fn [ctx rule form {:syms [?obj]}]
               (let [msg (str "Lock on a symbol bound to (Object.), not a " (name (simple-type ?obj)))]
                 (->diagnostic ctx rule form {:message msg})))})
