; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.clj-auth-test
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

(def clj-auth-diagnostics
  '{lint/defmethod-names 7
    lint/fn-wrapper 1
    lint/if-not-both 1
    lint/redundant-str-call 3
    lint/warn-on-reflection 19
    metrics/fn-length 17
    performance/dot-equals 27
    performance/single-literal-merge 7
    style/is-eq-order 6})

(defdescribe clj-auth-test
  (let [clj-auth (delay (gl/procure "https://github.com/theophilusx/clj-auth.git"
                          'theophilusx/clj-auth "4fbbf8222d92227c821c3db34c75a45d6e540185"))
        results (delay
                  (run-impl [{:path @clj-auth}]
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
         (m/equals clj-auth-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 88 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
