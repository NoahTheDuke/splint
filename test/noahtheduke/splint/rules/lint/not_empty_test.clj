; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.not-empty-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [extra]]
  (merge (single-rule-config 'lint/not-empty?)
         extra))

(defdescribe not-empty?-test
  (describe "chosen style:"
    (it ":seq"
      (expect-match
        [{:rule-name 'lint/not-empty?
          :form '(not (empty? x))
          :alt '(seq x)}]
        "(not (empty? x))"
        (config)))

    (it ":not-empty"
      (expect-match
        [{:rule-name 'lint/not-empty?
          :form '(not (empty? x))
          :alt '(not-empty x)}]
        "(not (empty? x))"
        (config '{lint/not-empty? {:chosen-style :not-empty}})))))
