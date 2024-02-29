; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.if-else-nil-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/if-else-nil))

(defexpect if-else-nil-test
  (expect-match
    [{:form '(if x y)
      :message "Use `when` which doesn't require specifying the else branch."
      :alt '(when x y)}]
    "(if x y)"
    (config))
  (expect-match
    [{:form '(if x y nil)
      :alt '(when x y)}]
    "(if x y nil)"
    (config))
  (expect-match
    [{:form '(if x (do y))
      :alt '(when x y)}]
    "(if x (do y))"
    (config))
  (expect-match nil "(if x y z)" (config)))

(defexpect rest-arg-list-test
  (expect-match
    '[{:alt (when x (if y (z a b c) d))}]
    "(if x (if y (z a b c) d))"
    (config)))
