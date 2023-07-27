; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.into-literal-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/into-literal))

(defexpect into-vec-test
  (expect-match
    '[{:alt (vec coll)}]
    "(into [] coll)"
    (config))
  (expect-match
    '[{:alt (vec (range 100))}]
    "(into [] (range 100))"
    (config))
  (expect-match
    nil
    "(into [] xf coll)"
    (config))
  (expect-match
    nil
    "(into [1 2] coll)"
    (config)))

(defexpect into-set-test
  (expect-match
    '[{:alt (set coll)}]
    "(into #{} coll)"
    (config))
  (expect-match
    '[{:alt (set (range 100))}]
    "(into #{} (range 100))"
    (config))
  (expect-match
    nil
    "(into #{} xf coll)"
    (config))
  (expect-match
    nil
    "(into #{1 2} coll)"
    (config)))
