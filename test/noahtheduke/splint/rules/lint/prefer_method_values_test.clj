; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.prefer-method-values-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config []
  (-> (single-rule-config 'lint/prefer-method-values)
    (assoc-in ['style/prefer-clj-string :enabled] true)
    (assoc :clojure-version {:major 1 :minor 12})))

(defdescribe prefer-method-values-test
  (it "respects dot method"
    (expect-match
      [{:rule-name 'lint/prefer-method-values
        :form '(.bar foo baz)
        :message "Prefer uniform Class/member syntax instead of traditional interop."
        :alt '(CLASS/.bar foo baz)}]
      "(.bar foo baz)"
      (config)))
  (it "respects period method"
    (expect-match
      [{:rule-name 'lint/prefer-method-values
        :form '(. Object (method) 1 2 3)
        :message "Prefer uniform Class/member syntax instead of traditional interop."
        :alt '(Object/method 1 2 3)}]
      "(ns foo (:import (java.lang Object))) (. Object (method) 1 2 3)"
      (config)))
  (it "doesn't work with lower clojure version"
    (expect-match
      nil
      "(.bar foo baz)"
      (assoc (config) :clojure-version {:major 1 :minor 11}))))
