; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.rand-int-one
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn low-num? [f]
  (or (#{0 0.0 -1 -1.0 1 1.0} f)
    (and (or (float? f)
           (double? f))
      (or (< (long f) 2)
        (< -2 (long f))))))

(defrule lint/rand-int-one
  "`clojure.core/rand-int` returns an integer between `0` (inclusive) and `n` (exclusive), meaning that a call to `(rand-int 1)` will always return `0`.

  Checks the following numbers: `0`, `0.0`, `1`, `1.0`, `-1`, `-1.0`

  @examples

  ; avoid
  (rand-int 0)
  (rand-int -1)
  (rand-int 1)
  (rand-int 1.0)
  (rand-int -1.0)
  (rand-int 1.5)
  "
  {:pattern '(rand-int (? f low-num?))
   :on-match (fn [ctx rule form {:syms [?f]}]
               (let [msg (format "Always returns 0. Did you mean (rand %s) or (rand-int 2)?" ?f)]
                 (->diagnostic ctx rule form {:message msg})))})
