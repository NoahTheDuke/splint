; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.conj-vector-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/conj-vector))

(deftest conj-vec-test
  (expect-match
    [{:form '(conj [] x)
      :message "Use `vector` instead of recreating it."
      :alt '(vector x)}]
    "(conj [] x)"
    (config)))
