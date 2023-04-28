; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.fn-wrapper-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-alt check-all]]))

(defexpect fn-wrapper-test
  (expect 'f (check-alt "(fn* [arg] (f arg))"))
  (expect 'f (check-alt "(fn [arg] (f arg))"))
  (expect 'f (check-alt "#(f %)")))

(defexpect interop-static-test
  (expect nil (check-alt "#(Integer/parseInt %)"))
  (expect nil (check-all "(do (import (java.util.regex Pattern)) #(Pattern/compile %))")))
