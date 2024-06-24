; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.let-when-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/let-when))

(defdescribe let-when-test
  (it "works with when"
    (expect-match
      [{:rule-name 'lint/let-when
        :form '(let [result (some-func)] (when result (do-stuff result)))
        :alt '(when-let [result (some-func)] (do-stuff result))}]
      "(let [result (some-func)] (when result (do-stuff result)))"
      (config)))
  (it "works with ifs with one branch"
    (expect-match
      [{:rule-name 'lint/let-when
        :form '(let [result (some-func)] (if result (do-stuff result)))
        :alt '(when-let [result (some-func)] (do-stuff result))}]
      "(let [result (some-func)] (if result (do-stuff result)))"
      (config)))
  (it "ignores ifs with 2 branches"
    (expect-match
      nil
      "(let [result (some-func)] (if result (do-stuff result) (some-func)))"
      (config))))
