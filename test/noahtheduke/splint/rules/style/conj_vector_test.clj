; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.conj-vector-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/conj-vector)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe conj-vec-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(conj [] x)
        :message "Use `vector` instead of recreating it."
        :alt '(vector x)}]
      "(conj [] x)"
      (config))))
