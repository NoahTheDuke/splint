; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.cond-else-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt]]))

(defexpect cond-else-test
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-alt "(cond (pos? x) (inc x) :default -1)"))
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-alt "(cond (pos? x) (inc x) true -1)"))
  (expect nil? (check-alt "(cond (pos? x) (inc x) (neg? x) (dec x))"))
  (expect nil? (check-alt "(cond :else true)")))
