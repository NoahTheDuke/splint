; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.single-key-in-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt check-message]]))

(defexpect single-key-in-test
  (expect '(assoc coll :k v) (check-alt "(assoc-in coll [:k] v)"))
  (expect "Use `assoc` instead of recreating it." (check-message "(assoc-in coll [:k] v)"))
  (expect '(get coll :k) (check-alt "(get-in coll [:k])"))
  (expect '(get coll :k :default) (check-alt "(get-in coll [:k] :default)"))
  (expect "Use `get` instead of recreating it." (check-message "(get-in coll [:k])"))
  (expect '(update coll :k inc) (check-alt "(update-in coll [:k] inc)"))
  (expect '(update coll :k + 1 2 3) (check-alt "(update-in coll [:k] + 1 2 3)"))
  (expect "Use `update` instead of recreating it." (check-message "(update-in coll [:k] inc)")))
