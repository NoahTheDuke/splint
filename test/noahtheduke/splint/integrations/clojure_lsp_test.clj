; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.clojure-lsp-test
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

(def clojure-lsp-diagnostics
  '{lint/body-unquote-splicing 2
    lint/catch-throwable 12
    lint/defmethod-names 137
    lint/empty-loop-in-fn 1
    lint/fn-wrapper 2
    lint/identical-branches 8
    lint/if-nil-else 3
    lint/if-not-both 3
    lint/into-literal 2
    lint/let-when 1
    lint/redundant-call 1
    lint/thread-macro-one-arg 116
    lint/warn-on-reflection 147
    metrics/fn-length 296
    metrics/parameter-count 47
    naming/conventional-aliases 56
    naming/conversion-functions 6
    naming/lisp-case 2
    naming/predicate 1
    naming/record-name 2
    naming/single-segment-namespace 3
    performance/assoc-many 38
    performance/dot-equals 193
    performance/get-keyword 8
    performance/into-transducer 1
    performance/single-literal-merge 5
    style/apply-str 3
    style/eq-false 26
    style/eq-nil 36
    style/eq-true 14
    style/eq-zero 32
    style/filter-complement 2
    style/new-object 2
    style/not-eq 3
    style/not-nil? 3
    style/not-some-pred 1
    style/plus-one 10
    style/pos-checks 4
    style/prefer-clj-string 4
    style/prefer-condp 3
    style/prefer-for-with-literals 2
    style/redundant-regex-constructor 1
    style/single-key-in 4
    style/tostring 20
    style/when-not-call 9})

(defdescribe clojure-lsp-test
  (let [clojure-lsp (delay (gl/procure "https://github.com/clojure-lsp/clojure-lsp.git"
                             'com.github.clojure-lsp/clojure-lsp "2025.08.25-14.21.46"))
        results (delay
                  (run-impl [{:path @clojure-lsp}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    ; (user/pprint (into (sorted-map) (dissoc @diagnostics 'lint/warn-on-reflection)))
    ; (user/pprint (into (sorted-map) (update-vals* @diagnostics count)))
    ; (user/pprint (get @diagnostics 'lint/empty-loop-in-fn))
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals clojure-lsp-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 1272 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))))
