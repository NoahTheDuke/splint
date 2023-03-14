; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.fn-wrapper-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect fn-wrapper-test
  (expect 'f (check-alt "(fn* [arg] (f arg))"))
  (expect 'f (check-alt "(fn [arg] (f arg))"))
  (expect 'f (check-alt "#(f %)")))
