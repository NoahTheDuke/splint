; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.useless-destructure-test
  (:require
   [lazytest.core :refer [defdescribe describe it specify]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/useless-destructure)

(defdescribe useless-destructure-test
  (describe "let-likes"
    (specify "let"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(let [a b {} c] d e f)"
        (single-rule-config rule-name)))
    (specify "if-let"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(if-let [{} c] d e)"
        (single-rule-config rule-name)))
    (specify "if-some"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(if-some [{} c] d e)"
        (single-rule-config rule-name)))
    (specify "when-let"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(when-let [{} c] d e)"
        (single-rule-config rule-name)))
    (specify "when-some"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(when-some [{} c] d e)"
        (single-rule-config rule-name)))
    (specify "loop"
      (expect-match
        [{:rule-name rule-name
          :form '[{} c]
          :message "Empty destructured binding"
          :alt nil}]
        "(loop [{} c] d e)"
        (single-rule-config rule-name)))
    (it "skips real destructures"
      (expect-match
        nil
        "(let [a b {:keys [q w]} c] d e f)"
        (single-rule-config rule-name))))
  (describe 'defn
    (it "works"
      (expect-match
        [{:rule-name rule-name
          :form '{}
          :line 1
          :column 17
          :message "Empty destructured parameter"
          :alt nil}]
        "(defn func [a b {} c] a b c)"
        (single-rule-config rule-name)))
    (it "skips real destructures"
      (expect-match
        nil
        "(defn func [a b {:keys [q w]} c] a b c)"
        (single-rule-config rule-name)))))
