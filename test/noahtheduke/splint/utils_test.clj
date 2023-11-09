; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.utils-test 
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.utils :as sut]))

(set! *warn-on-reflection* true)

(defexpect simple-type-test
  (doseq [[input t] '[[nil :nil]
                      [true :boolean]
                      [1 :number]
                      ["a" :string]
                      [:a :keyword]
                      [a :symbol]
                      [(1 2 3) :list]
                      [[1 2 3] :vector]
                      [{1 2} :map]
                      [#{1 2 3} :set]
                      [#"asdf" java.util.regex.Pattern]]]
    (expect (sut/simple-type input) t)))
