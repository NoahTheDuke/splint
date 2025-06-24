; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.misplaced-type-hint-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/misplaced-type-hint)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe misplaced-type-hint-test
  (it "handles single arities"
    (expect-match
    [{:rule-name rule-name
      :form (list 'defn (symbol "^String") 'make-str [] "abc")
      :alt (list 'defn 'make-str (symbol "^String") [] "abc")}]
    "(defn ^String make-str [] \"abc\")"
    (config)))
  (it "handles multi-arities"
    (expect-match
      [{:rule-name rule-name
        :form (list 'defn (symbol "^String") 'make-str '([] "abc") '([a] (str a "abc")))
        :alt (list 'defn 'make-str (list (symbol "^String") [] "abc")
                   (list (symbol "^String") '[a] '(str a "abc")))}]
      "(defn ^String make-str ([] \"abc\") ([a] (str a \"abc\")))"
      (config)))
  (it "ignores defns with no tag"
    (expect-match
      nil
      "(defn make-str [] \"abc\")"
      (config))
    (expect-match
      nil
      "(defn make-str ([] \"abc\") ([a] (str a \"abc\")))"
      (config))
    (expect-match
      nil
      "(defn make-str ^String [] \"abc\")"
      (config))))
