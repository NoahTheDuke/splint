; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.useless-do-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/useless-do)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe useless-do-x-test
  (it "works"
    (expect-match
      [{:form '(do x)
        :message "Unnecessary `do`."
        :alt 'x}]
      "(do x)"
      (config)))
  (it "ignores function literal context"
    (expect-match nil "#(do [%1 %2])" (config)))
  (it "ignores unquote-splicing context"
    (expect-match nil "(do ~@body)" (config))))
