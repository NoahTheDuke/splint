; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-clj-string-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-clj-string)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe prefer-clj-math-test
  (it "looks for .reverse cases"
    (expect-match
      [{:alt '(clojure.string/reverse "hello world")}]
      "(str (.reverse (StringBuilder. \"hello world\")))"
      (config)))
  (it "doesn't know how to avoid duplication"
    (expect-match
      [{:alt '(clojure.string/capitalize s)}
       {:rule-name rule-name
        :form '(.toUpperCase (subs s 0 1))
        :message "Use the `clojure.string` function instead of interop."
        :alt '(clojure.string/upper-case (subs s 0 1))
        :line 1
        :column 6
        :end-line 1
        :end-column 33}
       {:rule-name rule-name
        :form '(.toLowerCase (subs s 1))
        :message "Use the `clojure.string` function instead of interop."
        :alt '(clojure.string/lower-case (subs s 1))
        :line 1
        :column 34
        :end-line 1
        :end-column 59}]
      "(str (.toUpperCase (subs s 0 1)) (.toLowerCase (subs s 1)))"
      (config)))
  #_(expect-match
      '[{:alt (str x)}]
      "(.toString x)"))
