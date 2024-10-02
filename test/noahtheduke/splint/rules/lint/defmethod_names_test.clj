; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.defmethod-names-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/defmethod-names)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe defmethod-names-test
  (describe "works with literals"
    (it "nil"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi nil [arg1 arg2] (+ arg1 arg2))
          :message "Include a name for the method."
          :alt '(defmethod some-multi nil
                  some-multi--nil
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi nil [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "boolean"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi true [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi true
                  some-multi--true
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi true [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "char"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi \c [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi \c
                  some-multi--char-c
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi \\c [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "string"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi "foo" [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi "foo"
                  some-multi--foo
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi \"foo\" [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "symbol"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi 'a [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi 'a
                  some-multi--a
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi 'a [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "keyword"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi :foo [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi :foo
                  some-multi--foo
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi :foo [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "map"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi {:foo :bar} [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi {:foo :bar}
                  some-multi--foo-bar
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi {:foo :bar} [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "vector" 
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi [:foo :bar] [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi [:foo :bar]
                  some-multi--foo-bar
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi [:foo :bar] [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "set" 
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi #{:foo :bar} [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi #{:foo :bar}
                  some-multi--foo-bar
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi #{:foo :bar} [arg1 arg2] (+ arg1 arg2))"
        (config)))
    (it "regex"
      (expect-match
        [{:rule-name rule-name
          :form '(defmethod some-multi (splint/re-pattern "abc") [arg1 arg2] (+ arg1 arg2))
          :alt '(defmethod some-multi (splint/re-pattern "abc")
                  some-multi--regex-abc
                  [arg1 arg2] (+ arg1 arg2))}]
        "(defmethod some-multi #\"abc\" [arg1 arg2] (+ arg1 arg2))"
        (config))))
  (describe "ignores other types"
    (it "tagged literals"
      (expect-match
        nil
        "(defmethod some-multi #uuid \"abc\" [arg1 arg2] (+ arg1 arg2))"
        (config)))))
