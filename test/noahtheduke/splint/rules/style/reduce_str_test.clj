; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.reduce-str-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/reduce-str))

(defexpect reduce-str-test
  (expect-match
    [{:rule-name 'style/reduce-str
      :form '(reduce str x)
      :message "Use `clojure.string/join` for efficient string concatenation."
      :alt '(clojure.string/join x)}]
    "(reduce str x)"
    (config))
  (expect-match
    [{:form '(reduce str "" x)
      :alt '(clojure.string/join x)}]
    "(reduce str \"\" x)"
    (config)))
