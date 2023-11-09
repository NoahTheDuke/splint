; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.multiple-arity-order-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect multiple-arity-order-test
  (expect-match
    '[{:alt (defn foo
              ([x] (foo x 1))
              ([x y] (+ x y))
              ([x y & more] (reduce foo (+ x y) more)))}]
    "(defn foo
    ([x] (foo x 1))
    ([x y & more] (reduce foo (+ x y) more))
    ([x y] (+ x y)))")
  (expect-match
    nil
    "(defn foo ([a] 1) [a b])"))
