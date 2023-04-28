; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.set-literal-as-fn-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect set-literal-as-fn-test
  (expect '(case elem (a b c) elem nil)
    (check-alt "(#{'a 'b 'c} elem)"))
  (expect '(case elem (nil 1 :b c) elem nil)
    (check-alt "(#{nil 1 :b 'c} elem)"))
  (expect nil?
    (check-alt "(#{'a 'b c} elem)"))
  (expect nil?
    (check-alt "(#{'a 'b 'c '(1 2 3)} elem)")))
