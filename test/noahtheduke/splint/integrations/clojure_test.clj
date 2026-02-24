; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.clojure-test
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

(def clojure-diagnostics
  '{lint/assoc-fn 8
    lint/body-unquote-splicing 21
    lint/catch-throwable 21
    lint/defmethod-names 141
    lint/dot-class-method 8
    lint/dot-obj-method 163
    lint/fn-wrapper 4
    lint/identical-branches 7
    lint/if-else-nil 90
    lint/if-let-else-nil 4
    lint/if-nil-else 17
    lint/if-not-both 9
    lint/if-same-truthy 5
    lint/into-literal 6
    lint/let-if 19
    lint/let-when 3
    lint/loop-empty-when 2
    lint/misplaced-type-hint 70
    lint/no-catch 1
    lint/not-empty? 6
    lint/prefer-require-over-use 3
    lint/redundant-call 1
    lint/redundant-str-call 4
    lint/take-repeatedly 1
    lint/thread-macro-one-arg 14
    lint/try-splicing 8
    lint/warn-on-reflection 22
    metrics/fn-length 159
    metrics/parameter-count 19
    naming/conventional-aliases 6
    naming/conversion-functions 3
    naming/lisp-case 1
    naming/predicate 2
    performance/assoc-many 22
    performance/avoid-satisfies 1
    performance/dot-equals 24
    performance/get-keyword 2
    performance/single-literal-merge 3
    style/apply-str 29
    style/apply-str-interpose 7
    style/cond-else 14
    style/def-fn 30
    style/eq-zero 17
    style/filter-complement 4
    style/first-first 2
    style/first-next 2
    style/mapcat-apply-apply 1
    style/multiple-arity-order 5
    style/neg-checks 2
    style/new-object 83
    style/next-first 1
    style/next-next 4
    style/not-eq 11
    style/not-nil? 5
    style/plus-one 3
    style/pos-checks 7
    style/prefer-boolean 1
    style/prefer-clj-math 92
    style/prefer-clj-string 54
    style/prefer-condp 4
    style/prefer-for-with-literals 1
    style/prefer-var-dispatch 18
    style/prefer-vary-meta 1
    style/redundant-let 5
    style/defmulti-arglists 16
    style/redundant-nested-call 1
    style/single-key-in 1
    style/tostring 32
    style/useless-do 3
    style/when-do 2
    style/when-not-call 6
    style/when-not-empty? 1})

(defdescribe clojure-test
  (let [clojure (delay (gl/procure "https://github.com/clojure/clojure.git"
                             'org.clojure/clojure "clojure-1.12.1"))
        results (delay
                  (run-impl [{:path (str @clojure "/src")}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)))]
    ; (user/pprint (into (sorted-map) (dissoc @diagnostics 'lint/warn-on-reflection 'style/is-eq-order
    ;                'lint/defmethod-names 'lint/if-else-nil)))
    ; (user/pprint (into (sorted-map) (update-vals* @diagnostics count)))
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals clojure-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 1365 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
