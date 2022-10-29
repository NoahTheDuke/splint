; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test
             :refer [defexpect expect expecting]]
            [clj-kondo.impl.rewrite-clj.parser :as p]
            [noahtheduke.spat :refer [pattern]]))

(set! *warn-on-reflection* true)

(defexpect pattern-test
  (let [pat (pattern #{:a [2] [5] [3]})]
    (expect nil? (pat (p/parse-string "#{:a [2] [4] [3]}")))
    (expect (pat (p/parse-string "#{:a [2] [5] [3]}")))))

(defexpect literals-test
  (expect (pattern 'a) (p/parse-string "'a"))
  (expect (pattern :a) (p/parse-string ":a"))
  (expect (pattern "a") (p/parse-string "\"a\""))
  (expect (pattern 1) (p/parse-string "1"))
  (expect (pattern nil) (p/parse-string "nil")))
