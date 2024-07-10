; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.lisp-case-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'naming/lisp-case)

(defn config [& {:as style}]
  (cond-> (single-rule-config rule-name)
    style (update rule-name merge style)))

(defdescribe lisp-case-test
  (it "relies on camel-snake-kebab"
    (doseq [input ["(def someVar 1)"
                   "(def SomeVar 1)"
                   "(def some_var 1)"
                   "(def Some_var 1)"]]
      (expect-match
        '[{:alt (def some-var 1)}]
        input
        (config))))
  (it "checks defns too"
    (doseq [input ["(defn someVar [arg] 1)"
                   "(defn some_var [arg] 1)"]]
      (expect-match
        '[{:alt (defn some-var [arg] 1)}]
        input
        (config))))
  (it "doesn't get fancy"
    (doseq [input ["(def somevar 1)"
                   "(defn somevar [args] 1)"
                   "(def _somevar 1)"
                   "(defn _somevar [args] 1)"
                   "(def Somevar 1)"
                   "(defn Somevar [args] 1)"
                   "(list (def someVar 1))"]]
      (expect-match nil input (config)))))
