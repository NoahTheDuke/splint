; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.malapropism-test
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

(def malapropism-diagnostics
  '{lint/warn-on-reflection 4
    metrics/fn-length 1})

(defdescribe malapropism-test
  (let [malapropism (delay (gl/procure "https://github.com/dpassen/malapropism.git" 'org.passen/malapropism "0.6.241"))
        results (delay
                  (run-impl [{:path @malapropism}]
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
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals malapropism-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 5 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))))
