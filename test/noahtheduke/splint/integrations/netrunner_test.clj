; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.netrunner-test
  {:integration true}
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [noahtheduke.splint.integrations.helpers :refer [usefully-enabled-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def netrunner-diagnostics
  '{lint/assoc-fn 6
    lint/catch-throwable 1
    lint/defmethod-names 295
    lint/fn-wrapper 45
    lint/identical-branches 9
    lint/if-else-nil 13
    lint/if-let-else-nil 2
    lint/if-nil-else 5
    lint/if-not-both 27
    lint/if-same-truthy 5
    lint/into-literal 50
    lint/let-if 3
    lint/redundant-call 2
    lint/redundant-str-call 47
    lint/thread-macro-one-arg 93
    lint/warn-on-reflection 184
    metrics/fn-length 614
    metrics/parameter-count 249
    naming/conventional-aliases 34
    naming/conversion-functions 15
    naming/predicate 27
    performance/assoc-many 297
    performance/dot-equals 1256
    performance/get-keyword 4
    performance/into-transducer 4
    performance/single-literal-merge 17
    style/apply-str 1
    style/apply-str-interpose 2
    style/assoc-assoc 2
    style/def-fn 3
    style/eq-false 3
    style/eq-nil 17
    style/eq-true 2
    style/eq-zero 294
    style/filter-complement 28
    style/filter-vec-filterv 2
    style/first-first 2
    style/first-next 1
    style/is-eq-order 128
    style/minus-one 9
    style/multiple-arity-order 3
    style/multiply-by-one 1
    style/neg-checks 1
    style/new-object 2
    style/not-eq 19
    style/not-nil? 16
    style/not-some-pred 17
    style/plus-one 17
    style/pos-checks 2
    style/prefer-clj-math 18
    style/prefer-clj-string 5
    style/prefer-condp 4
    style/prefer-for-with-literals 1
    style/redundant-let 4
    style/redundant-nested-call 8
    style/redundant-regex-constructor 9
    style/single-key-in 41
    style/tostring 2
    style/useless-do 4
    style/when-do 4
    style/when-not-call 29
    style/when-not-empty? 13})

(defdescribe netrunner-test
  (let [netrunner (delay (gl/procure "https://github.com/mtgred/netrunner.git"
                           'mtgred/netrunner "v151"))
        results (delay
                  (run-impl [{:path @netrunner}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    ; (user/pprint (dissoc @diagnostics 'lint/warn-on-reflection))
    ; (user/pprint (into (sorted-map) (update-vals* @diagnostics count)))
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals netrunner-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 4018 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
