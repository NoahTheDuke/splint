; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.underscore-in-namespace-test
  (:require
   [expectations.clojure.test :refer [defexpect]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/underscore-in-namespace))

(defexpect underscore-in-namespace-test
  (expect-match
    [{:rule-name 'lint/underscore-in-namespace
      :form '(ns foo_bar.baz_qux)
      :message "Avoid underscores in namespaces."
      :alt 'foo-bar.baz-qux}]
    "(ns foo_bar.baz_qux)"
    (config))
  (expect-match
    [{:rule-name 'lint/underscore-in-namespace
      :form '(ns foo_bar.baz_qux (:require [clojure.string :as str]))
      :message "Avoid underscores in namespaces."
      :alt 'foo-bar.baz-qux}]
    "(ns foo_bar.baz_qux (:require [clojure.string :as str]))"
    (config))
  ;; only reports the namespace symbol itself
  (expect-match
    [{:line 3
      :column 3
      :end-line 3
      :end-column 18}]
    "\n(ns\n  foo_bar.baz_qux)"
    (config))
  (expect-match
    nil
    "(ns foo-bar.baz-qux)"
    (config)))
