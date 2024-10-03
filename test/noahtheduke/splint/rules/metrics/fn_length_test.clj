; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.metrics.fn-length-test
  (:require
   [lazytest.core :refer [defdescribe describe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [style]]
  (cond-> (single-rule-config 'metrics/fn-length)
    style (update 'metrics/fn-length merge style)))

(defdescribe fn-length-defn-test

  (describe "chosen style"
    (let [config (config {:chosen-style :defn})]
      (it :defn
        (expect-match nil "(defn n\n[]\n1 2 3)" config)
        (expect-match
          '[{:message "defn forms shouldn't be longer than 10 lines."}]
          "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
        (expect-match
          '[{:alt nil
             :line 1
             :column 1
             :end-line 13
             :end-column 4}]
          "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
        (expect-match
          '[{:alt nil
             :line 1
             :column 1
             :end-line 13
             :end-column 5}]
          "(defn n\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))" config)
        (expect-match
          '[{:alt nil
             :line 1
             :column 1
             :end-line 14
             :end-column 5}]
          "(defn n\n([] 1 2 3)\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))"
          config)))
    (let [config (config {:chosen-style :body})]
      (it :body
        (expect-match nil "(defn n\n[]\n1 2 3)" config)
        (expect-match
          '[{:message "Function bodies shouldn't be longer than 10 lines."}]
          "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
        (expect-match
          '[{:alt nil
             :line 2
             :column 1
             :end-line 13
             :end-column 3}]
          "(defn n\n[arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11)" config)
        (expect-match
          '[{:alt nil
             :line 2
             :column 1
             :end-line 13
             :end-column 4}]
          "(defn n\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))" config)
        (expect-match
          '[{:alt nil
             :line 3
             :column 1
             :end-line 14
             :end-column 4}]
          "(defn n\n([] 1 2 3)\n([arg1]\na\nb\nc\nd\n5\n6\n7\n8\n9\n10\n11))"
          config))))

  (describe "config length"
    (let [config (config {:length 5})]
      (it "custom length"
        (expect-match nil "(defn n\n[]\n0\n1\n2\n3)" config)
        (expect-match
          '[{:alt nil
             :message "Function bodies shouldn't be longer than 5 lines."
             :line 2
             :column 1
             :end-line 8
             :end-column 2}]
          "(defn n\n[]\n0\n1\n2\n3\n4\n5)" config)))))
