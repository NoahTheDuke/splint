; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.defmulti-arglists-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/defmulti-arglists)

(defdescribe defmulti-arglists-test
  (doseq [input ["(defmulti example :type)"
                 "(defmulti example \"docstring\" :type)"
                 "(defmulti example {:foo true} :type)"
                 "(defmulti example {:foo true} :type :hierarchy foo)"]]
    (it "works"
      (expect-match
        [{:rule-name rule-name
          :message "Specify the :arglists to help documentation and tooling."}]
        input
        (single-rule-config rule-name))))
  (doseq [input ["(defmulti example {:arglists '([obj])} :type)"
                 "(defmulti example {:arglists '([obj])} \"docstring\" :type)"
                 "(defmulti example {:arglists '([obj])} :type)"
                 "(defmulti example {:arglists '([obj]) :foo true} :type :hierarchy foo)"]]
    (it "skips when :arglists is included"
      (expect-match
        nil
        input
        (single-rule-config rule-name)))))
