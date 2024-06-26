; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.metrics.parameter-count-test
  (:require
   [lazytest.core :refer [defdescribe describe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [style]]
  (cond-> (single-rule-config 'metrics/parameter-count)
    style (update 'metrics/parameter-count merge style)))

(defdescribe parameter-count-test
  (it "defaults to 4"
    (expect-match
      [{:rule-name 'metrics/parameter-count
        :form '[a b c d e]
        :message "Avoid parameter lists with more than 4 parameters."
        :alt nil}]
      "(defn example ([a b c d]) ([a b c d e]))"
      '{metrics/parameter-count {:enabled true}}))

  (it "works with multiple arglists"
    (expect-match
      [{:rule-name 'metrics/parameter-count
        :form '[a b c d e f]
        :message "Avoid parameter lists with more than 4 parameters."
        :line 1
        :column 30
        :end-line 1
        :end-column 43}
       {:rule-name 'metrics/parameter-count
        :form '[a b c d e]
        :message "Avoid parameter lists with more than 4 parameters."
        :line 1
        :column 16
        :end-line 1
        :end-column 27}]
      "(defn example ([a b c d e]) ([a b c d e f]))"
      '{metrics/parameter-count {:enabled true}}))

  (describe "chosen styles"
    (it "custom count"
      (expect-match
        [{:rule-name 'metrics/parameter-count
          :form '[a b c d e f]
          :message "Avoid parameter lists with more than 5 parameters."
          :line 1
          :column 30
          :end-line 1
          :end-column 43}]
        "(defn example ([a b c d e]) ([a b c d e f]))"
        (config {:count 5})))

    (it :positional
      (expect-match
        [{:rule-name 'metrics/parameter-count
          :form '[a b c d e & args]
          :message "Avoid parameter lists with more than 4 parameters."}]
        "(defn example ([a b c d]) ([a b c d e & args]))"
        (config {:chosen-style :positional})))

    (it :include-rest
      (expect-match
        [{:rule-name 'metrics/parameter-count
          :form '[a b c d & args]
          :message "Avoid parameter lists with more than 4 parameters (including & rest parameters)."}]
        "(defn example ([a b c d]) ([a b c d & args]))"
        (config {:chosen-style :include-rest})))))
