; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dot-class-method-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/dot-class-method))

(defdescribe dot-class-usage-test
  (it "handles raw symbols"
    (expect-match
      [{:rule-name 'lint/dot-class-method
        :form '(. Object method 1 2 3)
        :message "Intention is clearer with `Obj/staticMethod` form."
        :alt '(Object/method 1 2 3)}]
      "(ns foo (:import (java.lang Object))) (. Object method 1 2 3)"
      (config)))
  (it "handles lists"
    (expect-match
      [{:rule-name 'lint/dot-class-method
        :form '(. Object (method) 1 2 3)
        :message "Intention is clearer with `Obj/staticMethod` form."
        :alt '(Object/method 1 2 3)}]
      "(ns foo (:import (java.lang Object))) (. Object (method) 1 2 3)"
      (config)))
  (it "respects lint/prefer-method-values"
    (expect-match
      [{:rule-name 'lint/prefer-method-values
        :form '(. Object (method) 1 2 3)
        :alt '(Object/method 1 2 3)}]
      "(ns foo (:import (java.lang Object))) (. Object (method) 1 2 3)"
      (-> (config)
          (assoc :clojure-version {:major 1 :minor 12})
          (assoc-in ['lint/prefer-method-values :enabled] true)))))
