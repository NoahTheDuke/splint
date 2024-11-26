; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.into-literal-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/into-literal)

(defdescribe into-literal-test

  (describe "vectors"
    (it "respects symbols"
      (expect-match
        [{:rule-name 'lint/into-literal
          :form '(into [] coll)
          :alt '(vec coll)}]
        "(into [] coll)"
        (single-rule-config rule-name)))
    (it "respects lists"
      (expect-match
        [{:rule-name 'lint/into-literal
          :form '(into [] (range 100))
          :alt '(vec (range 100))}]
        "(into [] (range 100))"
        (single-rule-config rule-name)))
    (it "ignores transducer arity"
      (expect-match
        nil
        "(into [] xf coll)"
        (single-rule-config rule-name)))
    (it "ignores non-empty vectors"
      (expect-match
        nil
        "(into [1 2] coll)"
        (single-rule-config rule-name))))

  (describe "sets"
    (it "respects symbols"
      (expect-match
        [{:rule-name 'lint/into-literal
          :form '(into #{} coll)
          :alt '(set coll)}]
        "(into #{} coll)"
        (single-rule-config rule-name)))
    (it "respects lists"
      (expect-match
        [{:rule-name 'lint/into-literal
          :form '(into #{} (range 100))
          :alt '(set (range 100))}]
        "(into #{} (range 100))"
        (single-rule-config rule-name)))
    (it "ignores transducer arity"
      (expect-match
        nil
        "(into #{} xf coll)"
        (single-rule-config rule-name)))
    (it "ignores non-empty sets"
      (expect-match
        nil
        "(into #{1 2} coll)"
        (single-rule-config rule-name))))

  (describe "ignores when threaded"
    (it "checks for ->"
      (expect-match
        nil
        "(-> (into [] coll))"
        (single-rule-config rule-name)))
    (it "checks for ->>"
      (expect-match
        nil
        "(->> (into [] coll))"
        (single-rule-config rule-name)))))
