; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test
             :refer [defexpect expect]]
            [clj-kondo.impl.rewrite-clj.parser :as p]
            [noahtheduke.spat :refer [pattern]]))

(set! *warn-on-reflection* true)

(defexpect literals-test
  (expect (pattern 'a) (p/parse-string "'a"))
  (expect (pattern :a) (p/parse-string ":a"))
  (expect (pattern "a") (p/parse-string "\"a\""))
  (expect (pattern 1) (p/parse-string "1"))
  (expect (pattern nil) (p/parse-string "nil")))

(defexpect list-test
  (let [pat (pattern (:a 1 :b [2] :c {:d 3}))]
    (expect not (pat (p/parse-string "(:a 1 :b [2] :c {:e 4})")))
    (expect (pat (p/parse-string "(:a 1 :b [2] :c {:d 3})")))
    (expect not (pat (p/parse-string "(:a 1 :b [2] :c {:d 3} :e 4)")))))

(defexpect map-test
  (let [pat (pattern {:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (p/parse-string "{:a 1 :b [2] :c {:e 4}}")))
    (expect (pat (p/parse-string "{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (p/parse-string "{:a 1 :b [2] :c {:d 3} :e 4}")))))

(defexpect set-test
  (let [pat (pattern #{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (p/parse-string "#{:a 1 :b [2] :c}")))
    (expect (pat (p/parse-string "#{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (p/parse-string "#{:a 1 :b [2] :c {:d 3} :e [4]}")))))

(defexpect vector-test
  (let [pat (pattern [:a 1 :b [2] :c {:d 3}])]
    (expect not (pat (p/parse-string "[:a 1 :b [2] :c {:e 4}]")))
    (expect (pat (p/parse-string "[:a 1 :b [2] :c {:d 3}]")))
    (expect not (pat (p/parse-string "[:a 1 :b [2] :c {:d 3} :e 4]")))))
