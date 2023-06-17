; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.re-frame-test
  (:require
    [clojure.tools.gitlibs :as gl]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.matchers :as m]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.config :refer [default-config]]
    [noahtheduke.splint.runner :refer [run-impl]]))

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
    style/eq-false 1
    style/eq-nil 2
    style/eq-true 2
    style/redundant-let 2
    style/set-literal-as-fn 1
    style/when-not-call 1})

(defexpect ^:integration re-frame-test
  (let [re-frame (gl/procure "https://github.com/day8/re-frame.git" 'day8/re-frame "v1.3.0")
        results (run-impl {:silent true :parallel false}
                          [re-frame]
                          all-enabled-config)]
    (expect
      (match?
        (m/equals re-frame-diagnostics)
        (->> results
             :diagnostics
             (group-by :rule-name)
             (#(update-vals % count)))))
    (expect 55 (count (:diagnostics results)))))
