; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.tostring-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/tostring))

(deftest str-to-string-test
  (expect-match
    [{:form '(.toString x)
      :message "Use `str` instead of interop."
      :alt '(str x)}]
    "(.toString x)"
    (config))
  (expect-match
    [{:form '(String/toString x)
      :message "Use `str` instead of interop."
      :alt '(str x)}]
    "(String/toString x)"
    (config)))
