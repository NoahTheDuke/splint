; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.rand-int-one
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn low-num? [f]
  (when (number? f)
    (<= -1 f 1)))

(defrule lint/rand-int-one
  "`clojure.core/rand-int` generates a float between `0` and `n` (exclusive) and then casts it to an integer. When given `1` (or a number less than `1`), `rand-int` will always return `0`.

  @examples

  ; avoid
  (rand-int 0)
  (rand-int -1)
  (rand-int 1)
  (rand-int 1.0)
  (rand-int -1.0)
  "
  {:pattern '(rand-int (? f low-num?))
   :on-match (fn [ctx rule form {:syms [?f]}]
               (let [msg (format "Always returns 0. Did you mean (rand %s) or (rand-int 2)?" ?f)]
                 (->diagnostic ctx rule form {:message msg})))})
