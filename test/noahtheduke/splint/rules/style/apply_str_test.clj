; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.apply-str-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/apply-str))

(defexpect apply-str-test
  (expect-match
    '[{:alt (clojure.string/join x)}]
    "(apply str x)"
    (config))
  (expect-match
    nil
    "(apply str (reverse x))"
    (config))
  (expect-match
    nil
    "(apply str (interpose x))"
    (config)))
