; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.fn-wrapper-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/fn-wrapper)

(defdescribe fn-wrapper-test
  (it "handles various anonymous functions"
    (expect-match '[{:alt f}] "(fn* [arg] (f arg))" (single-rule-config rule-name))
    (expect-match '[{:alt f}] "(fn [arg] (f arg))" (single-rule-config rule-name))
    (expect-match '[{:alt f}] "#(f %)" (single-rule-config rule-name)))

  (it "ignores interop functions"
    (expect-match nil "#(Integer/parseInt %)" (single-rule-config rule-name))
    (expect-match nil "(do (import (java.util.regex Pattern)) #(Pattern/compile %))" (single-rule-config rule-name))
    (expect-match nil "#(.getPath %)" (single-rule-config rule-name)))
  (it "handles fns to skip"
    (expect-match nil "(ns foo (:require [dev.nu.morse :as morse])) (add-tap (fn [x] (morse/inspect x)))"
      (single-rule-config rule-name {:names-to-skip #{'inspect}}))))
