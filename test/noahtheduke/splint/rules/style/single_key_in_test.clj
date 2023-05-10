; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.single-key-in-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect single-key-in-test
  (expect-match
    '[{:alt (assoc coll :k v)
       :message "Use `assoc` instead of recreating it." }]
    "(assoc-in coll [:k] v)")
  (expect-match
    '[{:alt (get coll :k)
       :message "Use `get` instead of recreating it."}]
    "(get-in coll [:k])")
  (expect-match
    '[{:alt (get coll :k :default)}]
    "(get-in coll [:k] :default)")
  (expect-match
    '[{:alt (update coll :k inc)
       :message "Use `update` instead of recreating it."}]
    "(update-in coll [:k] inc)")
  (expect-match
    '[{:alt (update coll :k + 1 2 3)}]
    "(update-in coll [:k] + 1 2 3)"))
