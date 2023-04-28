; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.when-not-empty-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect when-not-empty?-test
  (expect '(when (seq x) y) (check-alt "(when-not (empty? x) y)"))
  (expect nil? (check-alt "(if (= 1 called-with) \"arg\" \"args\")")))

