; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.intransit-test
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

(def intransit-diagnostics
  '{lint/warn-on-reflection 2
    metrics/fn-length 2})

(defdescribe intransit-test
  (let [intransit (delay (gl/procure "https://github.com/dpassen/intransit.git"
                          'terop/intransit "25c49ebdd15191bd9cc851cc18cf1013fd1ab49f"))
        results (delay
                  (run-impl [{:path @intransit}]
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
         (m/equals intransit-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 4 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
