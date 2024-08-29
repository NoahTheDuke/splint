; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.utils-test
  (:require
   [lazytest.core :refer [defdescribe expect it]]
   [noahtheduke.splint.utils :as sut]))

(set! *warn-on-reflection* true)

(defdescribe simple-type-test
  (map (fn [[input t]]
         (it (str t)
           (expect (sut/simple-type input) t)))
       '[[nil :nil]
         [true :boolean]
         [1 :number]
         ["a" :string]
         [:a :keyword]
         [a :symbol]
         [(1 2 3) :list]
         [[1 2 3] :vector]
         [{1 2} :map]
         [#{1 2 3} :set]
         [#"asdf" java.util.regex.Pattern]]))

(defdescribe support-clojure-version?-test
  (it "checks major, minor, and incremental"
    (expect
      (sut/support-clojure-version?
       {:major 1 :minor 12 :incremental 2}
       {:major 1 :minor 12 :incremental 2}))
    (expect
      (not
       (sut/support-clojure-version?
        {:major 1 :minor 12 :incremental 2}
        {:major 1 :minor 12 :incremental 1}))))
  (it "doesn't check minor if major is greater"
    (expect
      (sut/support-clojure-version?
       {:major 1 :minor 12}
       {:major 3 :minor 0})))
  (it "doesn't check incremental if minor is greater"
    (expect
      (sut/support-clojure-version?
       {:major 1 :minor 9 :incremental 100}
       {:major 1 :minor 12 :incremental 0}))))
