; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.netrunner-test
  {:integration true}
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe expect-it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [noahtheduke.splint.integrations.helpers :refer [usefully-enabled-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def netrunner-diagnostics
  '{lint/assoc-fn 6
    lint/catch-throwable 1
    lint/defmethod-names 294
    lint/fn-wrapper 44
    lint/identical-branches 11
    lint/if-else-nil 12
    lint/if-let-else-nil 2
    lint/if-nil-else 5
    lint/if-not-both 27
    lint/if-same-truthy 5
    lint/into-literal 49
    lint/let-if 3
    lint/let-when 1
    lint/missing-body-in-when 1
    lint/redundant-call 2
    lint/redundant-str-call 44
    lint/thread-macro-one-arg 95
    lint/warn-on-reflection 186
    metrics/fn-length 638
    metrics/parameter-count 255
    naming/conventional-aliases 34
    naming/conversion-functions 13
    naming/predicate 27
    performance/assoc-many 259
    performance/dot-equals 1269
    performance/get-keyword 6
    performance/into-transducer 9
    performance/single-literal-merge 17
    style/apply-str 1
    style/apply-str-interpose 2
    style/assoc-assoc 2
    style/def-fn 3
    style/defmulti-arglists 17
    style/eq-false 3
    style/eq-nil 17
    style/eq-true 2
    style/eq-zero 297
    style/filter-complement 29
    style/filter-vec-filterv 2
    style/first-first 2
    style/is-eq-order 128
    style/minus-one 10
    style/multiple-arity-order 3
    style/multiply-by-one 1
    style/neg-checks 1
    style/new-object 2
    style/not-eq 19
    style/not-nil? 16
    style/not-some-pred 17
    style/plus-one 17
    style/pos-checks 2
    style/prefer-boolean 1
    style/prefer-clj-math 18
    style/prefer-clj-string 5
    style/prefer-condp 4
    style/prefer-for-with-literals 1
    style/prefer-var-dispatch 13
    style/redundant-let 4
    style/redundant-nested-call 8
    style/redundant-regex-constructor 9
    style/single-key-in 41
    style/tostring 2
    style/useless-do 5
    style/when-do 4
    style/when-not-call 29
    style/when-not-empty? 13})

(defdescribe netrunner-test
  (let [netrunner (delay (gl/procure "https://github.com/mtgred/netrunner.git"
                           'mtgred/netrunner "v157.1"))
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
    (expect-it "has the right diagnostics"
      (match?
        (m/equals netrunner-diagnostics)
        (update-vals* @diagnostics count)))
    (expect-it "sums correctly"
      (= 4065 (count (:diagnostics @results))))
    (expect-it "raises no errors"
      (nil? (get diagnostics 'splint/error)))
    (expect-it "raises no unknown errors"
      (nil? (get diagnostics 'splint/unknown-error)))))
