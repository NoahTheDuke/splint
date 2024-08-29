; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-clj-math
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(def Math->clj-math
  '{IEEEremainder clojure.math/IEEE-remainder
    addExact clojure.math/add-exact
    copySign clojure.math/copy-sign
    decrementExact clojure.math/decrement-exact
    floorDiv clojure.math/floor-div
    floorMod clojure.math/floor-mod
    getExponent clojure.math/get-exponent
    incrementExact clojure.math/increment-exact
    multiplyExact clojure.math/multiply-exact
    negateExact clojure.math/negate-exact
    nextAfter clojure.math/next-after
    nextDown clojure.math/next-down
    nextUp clojure.math/next-up
    subtractExact clojure.math/subtract-exact
    toDegrees clojure.math/to-degrees
    toRadians clojure.math/to-radians
    Math/E clojure.math/E
    Math/IEEEremainder clojure.math/IEEE-remainder
    Math/PI clojure.math/PI
    Math/acos clojure.math/acos
    Math/addExact clojure.math/add-exact
    Math/asin clojure.math/asin
    Math/atan clojure.math/atan
    Math/atan2 clojure.math/atan2
    Math/cbrt clojure.math/cbrt
    Math/ceil clojure.math/ceil
    Math/copySign clojure.math/copy-sign
    Math/cos clojure.math/cos
    Math/cosh clojure.math/cosh
    Math/decrementExact clojure.math/decrement-exact
    Math/exp clojure.math/exp
    Math/expm1 clojure.math/expm1
    Math/floor clojure.math/floor
    Math/floorDiv clojure.math/floor-div
    Math/floorMod clojure.math/floor-mod
    Math/getExponent clojure.math/get-exponent
    Math/hypot clojure.math/hypot
    Math/incrementExact clojure.math/increment-exact
    Math/log clojure.math/log
    Math/log10 clojure.math/log10
    Math/log1p clojure.math/log1p
    Math/multiplyExact clojure.math/multiply-exact
    Math/negateExact clojure.math/negate-exact
    Math/nextAfter clojure.math/next-after
    Math/nextDown clojure.math/next-down
    Math/nextUp clojure.math/next-up
    Math/pow clojure.math/pow
    Math/random clojure.math/random
    Math/rint clojure.math/rint
    Math/round clojure.math/round
    Math/scalb clojure.math/scalb
    Math/signum clojure.math/signum
    Math/sin clojure.math/sin
    Math/sinh clojure.math/sinh
    Math/sqrt clojure.math/sqrt
    Math/subtractExact clojure.math/subtract-exact
    Math/tan clojure.math/tan
    Math/tanh clojure.math/tanh
    Math/toDegrees clojure.math/to-degrees
    Math/toRadians clojure.math/to-radians
    Math/ulp clojure.math/ulp})

(defn math? [sexp]
  (contains? Math->clj-math sexp))

(defrule style/prefer-clj-math
  "Prefer clojure.math to interop.

  @examples

  ; avoid
  Math/PI
  (Math/atan 45)

  ; prefer
  clojure.math/PI
  (clojure.math/atan 45)
  "
  {:pattern '(? sym math?)
   :init-type :symbol
   :message "Use the `clojure.math` function instead of interop."
   :min-clojure-version {:major 1 :minor 11}
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?sym]}]
               (let [new-form (Math->clj-math ?sym)]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
