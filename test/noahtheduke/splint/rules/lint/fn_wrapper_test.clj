; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.fn-wrapper-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect fn-wrapper-test
  (expect-match '[{:alt f}] "(fn* [arg] (f arg))")
  (expect-match '[{:alt f}] "(fn [arg] (f arg))")
  (expect-match '[{:alt f}] "#(f %)"))

(defexpect interop-static-test
  (expect-match nil "#(Integer/parseInt %)")
  (expect-match nil "(do (import (java.util.regex Pattern)) #(Pattern/compile %))")
  (expect-match nil "#(.getPath %)"))
