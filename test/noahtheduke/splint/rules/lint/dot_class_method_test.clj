; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.dot-class-method-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/dot-class-method))

(deftest dot-class-usage-test
  (expect-match
    [{:form '(. Object method 1 2 3)
      :message "Intention is clearer with `Obj/staticMethod` form."
      :alt '(Object/method 1 2 3)}]
    "(ns foo (:import (java.lang Object))) (. Object method 1 2 3)"
    (config))
  (expect-match
    [{:form '(. Object (method) 1 2 3)
      :message "Intention is clearer with `Obj/staticMethod` form."
      :alt '(Object/method 1 2 3)}]
    "(ns foo (:import (java.lang Object))) (. Object (method) 1 2 3)"
    (config)))
