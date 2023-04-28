; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.multiple-arity-order-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect multiple-arity-order-test
  (expect '(defn foo
             ([x] (foo x 1))
             ([x y] (+ x y))
             ([x y & more] (reduce foo (+ x y) more)))
    (check-alt "(defn foo
                  ([x] (foo x 1))
                  ([x y & more] (reduce foo (+ x y) more))
                  ([x y] (+ x y)))")))
