; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.warn-on-reflection-test
  (:require
   [clojure.java.io :as io]
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/warn-on-reflection))

(defdescribe warn-on-reflection-test
  (it "works"
    (expect-match
      [{:rule-name 'lint/warn-on-reflection
        :form nil
        :message "*warn-on-reflection* should be immediately after ns declaration."
        :alt nil
        :line 7
        :column 1
        :end-line 7
        :end-column 20
        :filename (io/file "corpus" "arglists.clj")}]
      (io/file "corpus" "arglists.clj")
      (config))
    (expect-match
      [{:rule-name 'lint/warn-on-reflection}]
      "(ns example) (+ 1 1)"
      (config)))
  (it "ignores when no namespaces"
    (expect-match nil "(+ 1 1)" (config)))
  (it "ignores when namespace is broken"
    (expect-match nil "(ns) (+ 1 1)" (config))))
