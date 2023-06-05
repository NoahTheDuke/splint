; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.warn-on-reflection-test
  (:require
    [clojure.java.io :as io]
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect warn-on-reflection-test
  (expect-match
    '[{:rule-name lint/warn-on-reflection
       :form (defn normal [a] a)
       :message "*warn-on-reflection* should be immediately after ns declaration."
       :alt nil
       :line 7
       :column 1
       :end-row 7
       :end-col 20
       :filename "corpus/arglists.clj"}]
    (io/file "corpus" "arglists.clj")
    '{lint/warn-on-reflection {:enabled true}
      naming/single-segment-namespace {:enabled false}}))
