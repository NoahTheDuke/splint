; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.trivial-for-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/trivial-for))

(defexpect trivial-for-test
  (expect-match
    [{:rule-name 'style/trivial-for
      :form '(for [item items] (f item))
      :message "Avoid trivial usage of `for`."
      :alt '(map f items)}]
    "(for [item items] (f item))"
    (config)))
