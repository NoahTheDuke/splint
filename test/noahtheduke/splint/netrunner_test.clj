; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.netrunner-test
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe it given expect]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.config :refer [default-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def all-enabled-config
  (update-vals @default-config #(assoc % :enabled true)))

(def netrunner-diagnostics
  '{lint/assoc-fn 3
    lint/fn-wrapper 37
    lint/if-else-nil 14
    lint/if-let-else-nil 2
    lint/if-nil-else 1
    lint/if-not-both 24
    lint/if-same-truthy 4
    lint/into-literal 40
    lint/let-if 3
    lint/let-when 1
    lint/redundant-call 3
    lint/redundant-str-call 58
    lint/thread-macro-one-arg 71
    lint/warn-on-reflection 169
    metrics/fn-length 485
    metrics/parameter-count 204
    naming/conventional-aliases 33
    naming/conversion-functions 11
    naming/predicate 15
    performance/assoc-many 244
    performance/dot-equals 1040
    performance/get-keyword 5
    performance/into-transducer 2
    performance/single-literal-merge 12
    style/apply-str 1
    style/apply-str-interpose 1
    style/assoc-assoc 4
    style/cond-else 3
    style/eq-false 3
    style/eq-nil 5
    style/eq-true 2
    style/eq-zero 232
    style/filter-complement 23
    style/filter-vec-filterv 1
    style/first-first 2
    style/first-next 1
    style/is-eq-order 17
    style/minus-one 7
    style/multiple-arity-order 2
    style/nested-addition 1
    style/new-object 1
    style/not-eq 18
    style/not-nil? 17
    style/not-some-pred 16
    style/plus-one 13
    style/pos-checks 4
    style/prefer-clj-math 16
    style/prefer-clj-string 5
    style/prefer-condp 2
    style/prefer-for-with-literals 1
    style/redundant-let 4
    style/redundant-regex-constructor 9
    style/set-literal-as-fn 24
    style/single-key-in 34
    style/tostring 2
    style/useless-do 2
    style/when-do 4
    style/when-not-call 27
    style/when-not-empty? 14})

(defdescribe ^:integration netrunner-test
  (given [netrunner (gl/procure "https://github.com/mtgred/netrunner.git" 'mtgred/netrunner "114")
        results (run-impl [{:path netrunner}]
                  {:config-override
                   (-> all-enabled-config
                     (assoc :silent true)
                     (assoc :parallel false)
                     (assoc :clojure-version *clojure-version*))})]
    (it "has the right failures"
      (expect
        (match?
          (m/equals netrunner-diagnostics)
          (->> results
               :diagnostics
               (group-by :rule-name)
               (#(update-vals % count)))))
      (expect 2998 (count (:diagnostics results)))
      (expect 222 (count (:checked-files results))))))
