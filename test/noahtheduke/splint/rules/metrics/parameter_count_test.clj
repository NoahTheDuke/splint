; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.metrics.parameter-count-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect parameter-count-test
  (expect-match
    [{:rule-name 'metrics/parameter-count
      :form '[a b c d e]
      :message "Avoid parameter lists with more than 4 parameters."
      :alt nil}]
    "(defn example ([a b c d]) ([a b c d e]))"
    '{metrics/parameter-count {:enabled true}}))

(defexpect parameter-count-multiple-arglists-test
  (expect-match
    [{:rule-name 'metrics/parameter-count
      :form '[a b c d e f]
      :message "Avoid parameter lists with more than 4 parameters."
      :line 1
      :column 30
      :end-row 1
      :end-col 43}
     {:rule-name 'metrics/parameter-count
      :form '[a b c d e]
      :message "Avoid parameter lists with more than 4 parameters."
      :line 1
      :column 16
      :end-row 1
      :end-col 27}]
    "(defn example ([a b c d e]) ([a b c d e f]))"
    '{metrics/parameter-count {:enabled true}}))

(defexpect parameter-count-config-count-test
  (expect-match
    [{:rule-name 'metrics/parameter-count
      :form '[a b c d e f]
      :message "Avoid parameter lists with more than 5 parameters."
      :line 1
      :column 30
      :end-row 1
      :end-col 43}]
    "(defn example ([a b c d e]) ([a b c d e f]))"
    '{metrics/parameter-count {:enabled true
                               :count 5}}))

(defexpect parameter-count-config-style-positional-test
  (expect-match
    [{:rule-name 'metrics/parameter-count
      :form '[a b c d e & args]
      :message "Avoid parameter lists with more than 4 parameters."}]
    "(defn example ([a b c d]) ([a b c d e & args]))"
    '{metrics/parameter-count {:enabled true
                               :chosen-style :positional}}))

(defexpect parameter-count-config-style-include-rest-test
  (expect-match
    [{:rule-name 'metrics/parameter-count
      :form '[a b c d & args]
      :message "Avoid parameter lists with more than 4 parameters (including & rest parameters)."}]
    "(defn example ([a b c d]) ([a b c d & args]))"
    '{metrics/parameter-count {:enabled true
                               :chosen-style :include-rest}}))
