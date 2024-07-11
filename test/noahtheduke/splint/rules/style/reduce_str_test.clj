; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.reduce-str-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/reduce-str)

(defdescribe reduce-str-test
  (it "works with no init-arg"
    (expect-match
      [{:rule-name rule-name
        :form '(reduce str x)
        :message "Use `clojure.string/join` for efficient string concatenation."
        :alt '(clojure.string/join x)}]
      "(reduce str x)"
      (single-rule-config rule-name)))
  (it "works with an empty init-arg"
    (expect-match
      [{:form '(reduce str "" x)
        :alt '(clojure.string/join x)}]
      "(reduce str \"\" x)"
      (single-rule-config rule-name)))
  (it "ignores a non-empty init-arg"
    (expect-match
      nil
      "(reduce str \"abc\" x)"
      (single-rule-config rule-name))))
