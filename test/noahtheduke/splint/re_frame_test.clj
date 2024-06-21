; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.re-frame-test
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe it expect given]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.config :refer [default-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def all-enabled-config
  (update-vals @default-config #(assoc % :enabled true)))

(def re-frame-diagnostics
  '{lint/if-else-nil 2
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
    style/set-literal-as-fn 1
    style/when-not-call 1})

(defdescribe re-frame-test
  {:integration true}
  (given [re-frame (gl/procure "https://github.com/day8/re-frame.git" 'day8/re-frame "v1.3.0")
          results (run-impl [{:path re-frame}]
                            {:config-override
                             (-> all-enabled-config
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version *clojure-version*))})]
    (it "hasn't changed"
      (expect
        (match?
          (m/equals re-frame-diagnostics)
          (->> results
               :diagnostics
               (group-by :rule-name)
               (#(update-vals % count)))))
      (expect 71 (count (:diagnostics results))))))
