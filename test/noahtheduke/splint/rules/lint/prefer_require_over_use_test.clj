; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.prefer-require-over-use-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-message]]))

(defexpect prefer-require-over-use-test
  (let [config '{lint/prefer-require-over-use {:chosen-style :as}}]
    (expect "Use (:require [some.lib :as l]) over (:use some.lib)"
      (check-message "(ns examples.ns (:use clojure.zip))" config)))
  (let [config '{lint/prefer-require-over-use {:chosen-style :refer}}]
      (expect "Use (:require [some.lib :refer [...]]) over (:use some.lib)"
        (check-message "(ns examples.ns (:use clojure.zip))" config)))
  (let [config '{lint/prefer-require-over-use {:chosen-style :all}}]
      (expect "Use (:require [some.lib :refer :all]) over (:use some.lib)"
        (check-message "(ns examples.ns (:use clojure.zip))" config))))
