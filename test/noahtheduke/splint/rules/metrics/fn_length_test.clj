; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.metrics.fn-length-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(def config
  '{metrics/fn-length {:enabled true}
    style/multiple-arity-order {:enabled false}})

(defexpect fn-length-defn-test
  (let [config (assoc-in config '[metrics/fn-length :chosen-style] :defn)]
    (expect-match nil "(defn n\n[]\n1 2 3)" config)
    (expect-match
      '[{:message "defn forms shouldn't be longer than 10 lines."}]
      "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
    (expect-match
      '[{:alt nil
         :line 1
         :column 1
         :end-row 13
         :end-col 4}]
      "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
    (expect-match
      '[{:alt nil
         :line 1
         :column 1
         :end-row 13
         :end-col 5}]
      "(defn n\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))" config)
    (expect-match
      '[{:alt nil
         :line 1
         :column 1
         :end-row 14
         :end-col 5}]
      "(defn n\n([] 1 2 3)\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))"
      config)))

(defexpect fn-length-body-test
  (let [config (assoc-in config '[metrics/fn-length :chosen-style] :body)]
    (expect-match nil "(defn n\n[]\n1 2 3)" config)
    (expect-match
      '[{:message "Function bodies shouldn't be longer than 10 lines."}]
      "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
    (expect-match
      '[{:alt nil
         :line 2
         :column 1
         :end-row 13
         :end-col 3}]
      "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
    (expect-match
      '[{:alt nil
         :line 2
         :column 1
         :end-row 13
         :end-col 4}]
      "(defn n\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))" config)
    (expect-match
      '[{:alt nil
         :line 3
         :column 1
         :end-row 14
         :end-col 4}]
      "(defn n\n([] 1 2 3)\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))"
      config)))

(defexpect fn-length-config-length-test
  (let [config (assoc-in config '[metrics/fn-length :length] 5)]
    (expect-match nil "(defn n\n[]\n0\n1\n2\n3)" config)
    (expect-match
      '[{:alt nil
         :message "Function bodies shouldn't be longer than 5 lines."
         :line 2
         :column 1
         :end-row 8
         :end-col 2}]
      "(defn n\n[]\n0\n1\n2\n3\n4\n5)" config)))
