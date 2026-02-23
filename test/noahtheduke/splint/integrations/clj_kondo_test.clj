; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.clj-kondo-test
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

(def clj-kondo-diagnostics
  '{lint/assoc-fn 1
    lint/body-unquote-splicing 3
    lint/catch-throwable 5
    lint/defmethod-names 6
    lint/dot-class-method 2
    lint/empty-loop-in-fn 1
    lint/fn-wrapper 1
    lint/identical-branches 2
    lint/if-else-nil 1
    lint/if-nil-else 1
    lint/if-not-both 2
    lint/let-if 3
    lint/let-when 1
    lint/missing-body-in-when 1
    lint/no-catch 1
    lint/no-op-assignment 2
    lint/redundant-str-call 1
    lint/thread-macro-one-arg 72
    lint/try-splicing 3
    lint/warn-on-reflection 115
    metrics/fn-length 235
    metrics/parameter-count 33
    naming/conventional-aliases 5
    naming/record-name 1
    performance/assoc-many 97
    performance/dot-equals 99
    performance/get-keyword 11
    performance/single-literal-merge 1
    style/apply-str 1
    style/apply-str-interpose 1
    style/eq-true 3
    style/multiple-arity-order 1
    style/neg-checks 1
    style/new-object 4
    style/not-eq 4
    style/not-some-pred 2
    style/pos-checks 1
    style/prefer-clj-string 5
    style/prefer-condp 2
    style/prefer-vary-meta 2
    style/single-key-in 1
    style/tostring 2
    style/useless-do 2
    style/when-not-call 13})

(defdescribe clj-kondo-test
  (let [clj-kondo (delay (gl/procure "https://github.com/clj-kondo/clj-kondo.git"
                           'clj-kondo/clj-kondo "v2025.10.23"))
        results (delay
                  (run-impl [{:path (str @clj-kondo "/src")}
                             {:path (str @clj-kondo "/test")}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 #_(assoc :autocorrect true)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    ; (user/pprint (into (sorted-map) (dissoc @diagnostics 'lint/warn-on-reflection 'lint/defmethod-names)))
    ; (user/pprint (into (sorted-map) (update-vals* @diagnostics count)))
    ; (user/pprint (get @diagnostics 'lint/empty-loop-in-fn))
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals clj-kondo-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 751 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get @diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get @diagnostics 'splint/unknown-error))))))
