; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.single-key-in-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'style/single-key-in))

(defexpect single-key-in-test
  (expect-match
    '[{:form (assoc-in coll [:k] v)
       :message "Use `assoc` instead of recreating it."
       :alt (assoc coll :k v)}]
    "(assoc-in coll [:k] v)"
    (config))
  (expect-match
    '[{:alt (get coll :k)
       :message "Use `get` instead of recreating it."}]
    "(get-in coll [:k])"
    (config))
  (expect-match
    '[{:alt (get coll :k :default)}]
    "(get-in coll [:k] :default)"
    (config))
  (expect-match
    '[{:alt (update coll :k inc)
       :message "Use `update` instead of recreating it."}]
    "(update-in coll [:k] inc)"
    (config))
  (expect-match
    '[{:alt (update coll :k + 1 2 3)}]
    "(update-in coll [:k] + 1 2 3)"
    (config)))
