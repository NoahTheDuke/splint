; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.fn-wrapper-test
  (:require
    [lazytest.core :refer [defdescribe it]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/fn-wrapper))

(defdescribe fn-wrapper-test
  (it "handles various anonymous functions"
    (expect-match '[{:alt f}] "(fn* [arg] (f arg))" (config))
    (expect-match '[{:alt f}] "(fn [arg] (f arg))" (config))
    (expect-match '[{:alt f}] "#(f %)" (config)))

  (it "ignores interop functions"
    (expect-match nil "#(Integer/parseInt %)" (config))
    (expect-match nil "(do (import (java.util.regex Pattern)) #(Pattern/compile %))" (config))
    (expect-match nil "#(.getPath %)" (config))))
