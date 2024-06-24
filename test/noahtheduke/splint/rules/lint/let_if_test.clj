; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.let-if-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/let-if))

(defdescribe let-if-test
  (it "works with else branch"
    (expect-match
      [{:rule-name 'lint/let-if
        :form '(let [result (some-func)] (if result (do-stuff result) (other-stuff)))
        :alt '(if-let [result (some-func)] (do-stuff result) (other-stuff))}]
      "(let [result (some-func)] (if result (do-stuff result) (other-stuff)))"
      (config)))
  (it "ignores no else branch"
    (expect-match nil
      "(let [result (some-func)] (if result (do-stuff result)))"
      (config))))
