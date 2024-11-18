; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.redundant-nested-call-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.rules.style.redundant-nested-call :as sut]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]
   [clojure.java.io :as io]
   [noahtheduke.splint.dev :as dev]))

(set! *warn-on-reflection* true)

(def rule-name 'style/redundant-nested-call)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe redundant-nested-call-test
  (it "handles specified vars"
    (doseq [input sut/relevant-call?]
      (expect-match
        [{:rule-name rule-name
          :form (list input 1 2 (list input 3 4))
          :message (format "Redundant nested call: `%s`." (str input))
          :alt (list input 1 2 3 4)}]
        (format "(%s 1 2 (%s 3 4))" input input)
        (config))))
  (it "doesn't match when nested calls are separated"
    (expect-match
      nil
      "(+ 1 2 (foo 3 4 (+ 5 6)))"
      (config)))
  (it "skips other vars"
    (expect-match
      nil
      "(foo 1 2 (foo 3 4))"
      (config))))

(defdescribe corpus-test
  (it "handles complex interactions"
    (expect-match
      [{:rule-name 'style/redundant-nested-call
        :form '(+ 1 2 (+ 3 4))
        :message "Redundant nested call: `+`."
        :alt '(+ 1 2 3 4)
        :line 10}
       {:rule-name 'style/redundant-nested-call
        :form '(str (some-call) (str (another-call)))
        :message "Redundant nested call: `str`."
        :alt '(str (some-call) (another-call))
        :line 12}
       {:rule-name 'lint/redundant-str-call
        :form '(str (str (another-call)))
        :message "`str` unconditionally returns a string."
        :alt '(str (another-call))
        :line 14}]
      (io/file "corpus" "nested_calls.clj")
      (update-vals @dev/dev-config #(assoc % :enabled true)))))
