; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.existing-constant-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/existing-constant)

(defn config-with-version
  [minor]
  (assoc (single-rule-config rule-name) :clojure-version {:major 1 :minor minor}))

(defdescribe existing-constant-test
  (describe "with clojure 1.11+"
    (let [version 11]
      (describe "PI"
        (it "checks for 3 digits minimum"
          (expect-match
            nil
            "(def pi 3.1)"
            (config-with-version version))
          (expect-match
            [{:rule-name rule-name
              :form '(def pi 3.14)
              :message "Use clojure.math/PI directly"
              :alt 'clojure.math/PI}]
            "(def pi 3.14)"
            (config-with-version version)))
        (it "handles longer numbers"
          (expect-match
            [{:rule-name rule-name
              :form '(def pi 3.14159)
              :message "Use clojure.math/PI directly"
              :alt 'clojure.math/PI}]
            "(def pi 3.14159)"
            (config-with-version version))))
      (describe "E"
        (it "checks for 4 digits minimum"
          (expect-match
            nil
            "(def e 2.71)"
            (config-with-version version))
          (expect-match
            [{:rule-name rule-name
              :form '(def e 2.718)
              :message "Use clojure.math/E directly"
              :alt 'clojure.math/E}]
            "(def e 2.718)"
            (config-with-version version)))
        (it "handles longer numbers too"
          (expect-match
            [{:rule-name rule-name
              :form '(def e 2.71828182)
              :message "Use clojure.math/E directly"
              :alt 'clojure.math/E}]
            "(def e 2.71828182)"
            (config-with-version version))))))
  (describe "with clojure 1.10"
    (let [version 10]
      (describe "PI"
        (it "checks for 3 digits minimum"
          (expect-match
            nil
            "(def pi 3.1)"
            (config-with-version version))
          (expect-match
            [{:rule-name rule-name
              :form '(def pi 3.14)
              :message "Use java.lang.Math/PI directly"
              :alt 'java.lang.Math/PI}]
            "(def pi 3.14)"
            (config-with-version version)))
        (it "handles longer numbers"
          (expect-match
            [{:rule-name rule-name
              :form '(def pi 3.14159)
              :message "Use java.lang.Math/PI directly"
              :alt 'java.lang.Math/PI}]
            "(def pi 3.14159)"
            (config-with-version version))))
      (describe "E"
        (it "checks for 4 digits minimum"
          (expect-match
            nil
            "(def e 2.71)"
            (config-with-version version))
          (expect-match
            [{:rule-name rule-name
              :form '(def e 2.718)
              :message "Use java.lang.Math/E directly"
              :alt 'java.lang.Math/E}]
            "(def e 2.718)"
            (config-with-version version)))
        (it "handles longer numbers too"
          (expect-match
            [{:rule-name rule-name
              :form '(def e 2.71828182)
              :message "Use java.lang.Math/E directly"
              :alt 'java.lang.Math/E}]
            "(def e 2.71828182)"
            (config-with-version version)))))))
