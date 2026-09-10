; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.useless-catch-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/useless-catch)

(defdescribe useless-catch-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(catch Exception ex (throw ex))
        :message "Useless catch"
        :line 1
        :column 17
        :alt nil}]
      "(try (do-stuff) (catch Exception ex (throw ex)))"
      (single-rule-config rule-name)))
  (it "skips when there's other entries"
    (expect-match
      nil
      "(try (do-stuff) (catch Exception ex (log/error ex) (throw ex)))"
      (single-rule-config rule-name))))
