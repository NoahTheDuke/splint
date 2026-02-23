; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.re-frame-test
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

(def re-frame-diagnostics
  '{lint/body-unquote-splicing 1
    lint/if-else-nil 2
    lint/if-let-else-nil 2
    lint/thread-macro-one-arg 15
    lint/try-splicing 1
    lint/warn-on-reflection 4
    metrics/fn-length 20
    naming/conventional-aliases 2
    naming/predicate 1
    naming/single-segment-namespace 1
    performance/assoc-many 2
    performance/avoid-satisfies 2
    performance/single-literal-merge 1
    style/eq-false 1
    style/eq-nil 2
    style/eq-true 2
    style/is-eq-order 10
    style/reduce-str 1
    style/redundant-let 2
    style/when-not-call 1})

(defdescribe re-frame-test
  (let [re-frame (delay (gl/procure "https://github.com/day8/re-frame.git" 'day8/re-frame "v1.3.0"))
        results (delay
                  (run-impl [{:path @re-frame}]
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
         (m/equals re-frame-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 73 (count (:diagnostics @results)))))))
