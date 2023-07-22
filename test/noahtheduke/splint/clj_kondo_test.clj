; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.clj-kondo-test
  (:require
    [clojure.tools.gitlibs :as gl]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.matchers :as m]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.config :refer [default-config]]
    [noahtheduke.splint.runner :refer [run-impl]]))

(def all-enabled-config
  (update-vals @default-config #(assoc % :enabled true)))

(def clj-kondo-diagnostics
  '{lint/assoc-fn 1
    lint/body-unquote-splicing 2
    lint/dot-class-method 3
    lint/dot-obj-method 1
    lint/fn-wrapper 1
    lint/if-else-nil 42
    lint/if-nil-else 2
    lint/if-not-both 3
    lint/into-literal 1
    lint/let-if 8
    lint/let-when 2
    lint/missing-body-in-when 2
    lint/prefer-require-over-use 4
    lint/thread-macro-one-arg 85
    lint/try-splicing 2
    lint/warn-on-reflection 229
    metrics/fn-length 314
    metrics/parameter-count 48
    naming/conventional-aliases 14
    naming/lisp-case 3
    naming/predicate 2
    naming/record-name 1
    naming/single-segment-namespace 80
    splint/parsing-error 4
    style/apply-str 14
    style/apply-str-interpose 3
    style/cond-else 8
    style/def-fn 2
    style/eq-false 2
    style/eq-true 5
    style/eq-zero 2
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
    style/set-literal-as-fn 2
    style/single-key-in 2
    style/tostring 4
    style/useless-do 6
    style/when-do 1
    style/when-not-call 15
    style/when-not-do 1})

(defexpect ^:integration clj-kondo-test
  (let [clj-kondo (gl/procure "https://github.com/clj-kondo/clj-kondo.git" 'clj-kondo/clj-kondo "v2023.05.26")
        results (run-impl [clj-kondo]
                          (assoc all-enabled-config
                                 :silent true
                                 :parallel false
                                 :clojure-version *clojure-version*))]
    (expect
      (match?
        (m/equals clj-kondo-diagnostics)
        (->> results
             :diagnostics
             (group-by :rule-name)
             (#(update-vals % count)))))
    (expect 989 (count (:diagnostics results)))))
