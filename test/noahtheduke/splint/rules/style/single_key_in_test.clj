; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.single-key-in-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/single-key-in)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe single-key-in-test
  (it assoc-in
    (expect-match
      '[{:form (assoc-in coll [:k] v)
         :message "Use `assoc` instead of recreating it."
         :alt (assoc coll :k v)}]
      "(assoc-in coll [:k] v)"
      (config)))
  (it get-in
    (expect-match
      '[{:alt (get coll :k)
         :message "Use `get` instead of recreating it."}]
      "(get-in coll [:k])"
      (config)))
  (it "get-in with default"
    (expect-match
      '[{:alt (get coll :k :default)}]
      "(get-in coll [:k] :default)"
      (config)))
  (it update-in
    (expect-match
      '[{:alt (update coll :k inc)
         :message "Use `update` instead of recreating it."}]
      "(update-in coll [:k] inc)"
      (config)))
  (it "update-in with varargs"
    (expect-match
      '[{:alt (update coll :k + 1 2 3)}]
      "(update-in coll [:k] + 1 2 3)"
      (config))))
