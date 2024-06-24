; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.prefer-require-over-use-test
  (:require
   [lazytest.core :refer [defdescribe describe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [extra]]
  (merge (single-rule-config 'lint/prefer-require-over-use) extra))

(defdescribe prefer-require-over-use-test
  (describe "chosen styles"
    (it ":as"
      (expect-match
        [{:rule-name 'lint/prefer-require-over-use
          :form '(ns examples.ns (:use clojure.zip))
          :alt nil
          :message "Use (:require [some.lib :as l]) over (:use some.lib)"}]
        "(ns examples.ns (:use clojure.zip))"
        (config {'lint/prefer-require-over-use {:chosen-style :as}})))
    (it ":refer"
      (expect-match
        [{:alt nil
          :message "Use (:require [some.lib :refer [...]]) over (:use some.lib)"}]
        "(ns examples.ns (:use clojure.zip))"
        (config {'lint/prefer-require-over-use {:chosen-style :refer}})))
    (it ":all"
      (expect-match
        [{:alt nil
          :message "Use (:require [some.lib :refer :all]) over (:use some.lib)"}]
        "(ns examples.ns (:use clojure.zip))"
        (config {'lint/prefer-require-over-use {:chosen-style :all}})))))
