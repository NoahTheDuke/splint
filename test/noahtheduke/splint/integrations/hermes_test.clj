; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.hermes-test
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

(def hermes-diagnostics
  '{lint/catch-throwable 2
    lint/defmethod-names 60
    lint/identical-branches 2
    lint/thread-macro-one-arg 32
    lint/warn-on-reflection 31
    metrics/fn-length 89
    metrics/parameter-count 13
    naming/conventional-aliases 9
    naming/conversion-functions 2
    naming/lisp-case 224
    naming/record-name 5
    performance/assoc-many 17
    performance/dot-equals 70
    performance/get-keyword 4
    performance/single-literal-merge 3
    style/apply-str 2
    style/is-eq-order 15
    style/minus-one 1
    style/multiple-arity-order 1
    style/not-some-pred 2
    style/prefer-clj-math 3
    style/prefer-clj-string 4
    style/prefer-for-with-literals 4
    style/tostring 3})

(defdescribe hermes-test
  (let [hermes (delay (gl/procure "https://github.com/wardle/hermes.git" 'com.heldrix/hermes "77ede487fd3c7b7f438d227dbdd9334db334ef48"))
        results (delay
                  (run-impl [{:path @hermes}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals hermes-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 598 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))))
