; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.netrunner-test
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [noahtheduke.splint.config :refer [default-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def all-enabled-config
  (-> @default-config
      (update-vals* #(assoc % :enabled true))
      (assoc-in ['style/set-literal-as-fn :enabled] false)))

(def netrunner-diagnostics
  '{lint/assoc-fn 4
    lint/catch-throwable 1
    lint/defmethod-names 265
    lint/fn-wrapper 42
    lint/if-else-nil 13
    lint/if-let-else-nil 2
    lint/if-nil-else 4
    lint/if-not-both 26
    lint/if-same-truthy 5
    lint/into-literal 42
    lint/let-if 3
    lint/redundant-call 2
    lint/redundant-str-call 44
    lint/thread-macro-one-arg 78
    lint/warn-on-reflection 176
    metrics/fn-length 548
    metrics/parameter-count 225
    naming/conventional-aliases 31
    naming/conversion-functions 13
    naming/predicate 24
    performance/assoc-many 272
    performance/dot-equals 1200
    performance/get-keyword 7
    performance/into-transducer 5
    performance/single-literal-merge 13
    style/apply-str 1
    style/apply-str-interpose 1
    style/assoc-assoc 2
    style/eq-false 3
    style/eq-nil 15
    style/eq-true 2
    style/eq-zero 274
    style/filter-complement 25
    style/filter-vec-filterv 2
    style/first-first 2
    style/first-next 1
    style/is-eq-order 127
    style/minus-one 8
    style/multiple-arity-order 2
    style/multiply-by-one 1
    style/nested-addition 1
    style/new-object 2
    style/not-eq 19
    style/not-nil? 16
    style/not-some-pred 17
    style/plus-one 15
    style/pos-checks 4
    style/prefer-boolean 1
    style/prefer-clj-math 18
    style/prefer-clj-string 5
    style/prefer-condp 5
    style/prefer-for-with-literals 1
    style/redundant-nested-call 10
    style/redundant-regex-constructor 9
    style/single-key-in 36
    style/tostring 2
    style/useless-do 3
    style/when-do 4
    style/when-not-call 28
    style/when-not-empty? 13})

(defdescribe ^:integration netrunner-test
  (let [netrunner (delay (gl/procure "https://github.com/mtgred/netrunner.git" 'mtgred/netrunner "v134"))
        results (delay
                  (run-impl [{:path @netrunner}]
                            {:config-override
                             (-> all-enabled-config
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals netrunner-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 3720 (count (:diagnostics @results)))))
    (it "checks the correct number of files"
      (expect (= 242 (count (:checked-files @results)))))))
