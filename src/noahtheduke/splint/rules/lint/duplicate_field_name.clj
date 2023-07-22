; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.duplicate-field-name
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/duplicate-field-name
  "`deftype` and `defrecord` will throw errors if you define multiple fields
  with the same name, but it's good to catch these things early too.

  Examples:

  # bad
  (defrecord Foo [a b a])

  # good
  (defrecord Foo [a b c])
  "
  {:pattern2 '(defrecord ?name (? fields vector?) ?*body)
   :on-match (fn [ctx rule form {:syms [?fields]}]
               (when (not= (count ?fields) (count (set ?fields)))
                 (->diagnostic ctx rule form {:message "Duplicate field has been found"})))})
