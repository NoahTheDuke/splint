; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.ktra-indexer-test
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

(def ktra-indexer-diagnostics
  '{lint/warn-on-reflection 11
    metrics/fn-length 15
    performance/dot-equals 13
    performance/single-literal-merge 2
    style/prefixed-libspecs 7})

(defdescribe ktra-indexer-test
  (let [ktra-indexer (delay (gl/procure "https://github.com/terop/ktra-indexer.git"
                          'terop/ktra-indexer "af2008c12a64af81350875e1aef7dbf43cb60303"))
        results (delay
                  (run-impl [{:path @ktra-indexer}]
                            {:config-override
                             (-> (usefully-enabled-config)
                                 (assoc :silent true)
                                 (assoc :parallel false)
                                 (assoc :clojure-version {:major 1 :minor 11}))}))
        diagnostics (delay (->> @results
                                :diagnostics
                                (group-by :rule-name)
                                (into (sorted-map))))]
    ; (user/pprint (dissoc @diagnostics 'lint/warn-on-reflection))
    ; (user/pprint (into (sorted-map) (update-vals* @diagnostics count)))
    (it "has the right diagnostics"
      (expect
        (match?
         (m/equals ktra-indexer-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 48 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
