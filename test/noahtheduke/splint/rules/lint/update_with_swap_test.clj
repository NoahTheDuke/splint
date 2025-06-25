; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.update-with-swap-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'lint/update-with-swap)

(defdescribe update-with-swap-test
  (it "works with update"
    (expect-match
      [{:rule-name rule-name
        :form '(update state :counter swap! + 5)
        :message "swap! in update derefs the value in the map."
        :alt '(swap! (:counter state) + 5)}]
      "(update state :counter swap! + 5)"
      (single-rule-config rule-name)))
  (it "works with update-in"
    (expect-match
      [{:rule-name rule-name
        :form '(update-in state [:users :counter] swap! + 5)
        :message "swap! in update derefs the value in the map."
        :alt '(swap! (get-in state [:users :counter]) + 5)}]
      "(update-in state [:users :counter] swap! + 5)"
      (single-rule-config rule-name)))
  (it "expects at least 1 arg to swap!"
    (expect-match
      nil
      "(update state :counter swap!)"
      (single-rule-config rule-name)))
  (it "doesn't care about the args to swap!"
    (expect-match
      [{:rule-name rule-name
        :form '(update state :counter swap! (fn [foo] foo))
        :message "swap! in update derefs the value in the map."
        :alt '(swap! (:counter state) (fn [foo] foo))}]
      "(update state :counter swap! (fn [foo] foo))"
      (single-rule-config rule-name))))
