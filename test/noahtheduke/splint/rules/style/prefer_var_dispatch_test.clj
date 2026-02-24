; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-var-dispatch-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefer-var-dispatch)

(defdescribe prefer-var-dispatch-test
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '(defmulti example :type)
        :message "Use a var for the dispatch function to improve repl-drive development."}]
      "(defmulti example :type)"
      (single-rule-config rule-name)))
  (it "can handle docstrings"
    (expect-match
      nil
      "(defmulti print-find \"heck\" #'print-find-dispatch)"
      (single-rule-config rule-name)))
  (it "can handle attr maps"
    (expect-match
      nil
      "(defmulti print-find {:arglists '([output diagnostic])} #'print-find-dispatch)"
      (single-rule-config rule-name)))
  (it "can handle both docstrings and attr maps"
    (expect-match
      nil
      "(defmulti print-find \"heck\" {:arglists '([output diagnostic])} #'print-find-dispatch)"
      (single-rule-config rule-name))))
