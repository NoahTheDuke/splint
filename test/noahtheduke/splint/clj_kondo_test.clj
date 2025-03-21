; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.clj-kondo-test
  (:require
   [clojure.tools.gitlibs :as gl]
   [lazytest.core :refer [defdescribe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [matcher-combinators.matchers :as m]
   [noahtheduke.splint.config :refer [default-config]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(def all-enabled-config
  (-> @default-config
      (update-vals* #(assoc % :enabled true))
      (assoc-in ['style/set-literal-as-fn :enabled] false)))

(def clj-kondo-diagnostics
  '{lint/assoc-fn 1
    lint/body-unquote-splicing 2
    lint/defmethod-names 63
    lint/dot-class-method 2
    lint/dot-obj-method 1
    lint/fn-wrapper 1
    lint/if-else-nil 42
    lint/if-let-else-nil 2
    lint/if-nil-else 2
    lint/if-not-both 3
    lint/let-if 8
    lint/let-when 2
    lint/misplaced-type-hint 10
    lint/missing-body-in-when 2
    lint/prefer-require-over-use 4
    lint/redundant-str-call 18
    lint/thread-macro-one-arg 85
    lint/try-splicing 2
    lint/underscore-in-namespace 1
    lint/warn-on-reflection 229
    metrics/fn-length 314
    metrics/parameter-count 48
    naming/conventional-aliases 14
    naming/conversion-functions 2
    naming/lisp-case 3
    naming/predicate 2
    naming/record-name 1
    naming/single-segment-namespace 80
    performance/assoc-many 80
    performance/avoid-satisfies 2
    performance/dot-equals 123
    performance/get-keyword 14
    performance/single-literal-merge 9
    splint/parsing-error 4
    style/apply-str 14
    style/apply-str-interpose 3
    style/cond-else 5
    style/def-fn 3
    style/eq-false 2
    style/eq-true 5
    style/eq-zero 2
    style/is-eq-order 27
    style/multiple-arity-order 1
    style/neg-checks 7
    style/new-object 5
    style/not-eq 3
    style/not-nil? 12
    style/not-some-pred 2
    style/plus-one 4
    style/pos-checks 1
    style/prefer-clj-string 18
    style/prefer-condp 3
    style/prefer-vary-meta 6
    style/redundant-let 6
    style/redundant-nested-call 6
    style/single-key-in 2
    style/tostring 4
    style/useless-do 6
    style/when-do 1
    style/when-not-call 15
    style/when-not-do 1})

(defdescribe ^:integration clj-kondo-test
  (let [clj-kondo (delay (gl/procure "https://github.com/clj-kondo/clj-kondo.git" 'clj-kondo/clj-kondo "v2023.05.26"))
        results (delay
                  (run-impl [{:path @clj-kondo}]
                            {:config-override
                             (-> all-enabled-config
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 #_(assoc :autocorrect true)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals clj-kondo-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 1340 (count (:diagnostics @results)))))))
