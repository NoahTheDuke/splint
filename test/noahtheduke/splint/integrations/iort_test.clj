; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.iort-test
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

(def iort-diagnostics
  '{lint/defmethod-names 10
    lint/warn-on-reflection 8
    metrics/fn-length 9
    naming/lisp-case 1
    performance/assoc-many 1
    performance/dot-equals 3
    style/first-first 1
    style/not-some-pred 1})

(defdescribe iort-test
  (let [iort (delay (gl/procure "https://github.com/wardle/iort.git"
                          'com.eldrix/iort "4b591d6d5f5f7fea5e76c0e7ab445aa0addcb421"))
        results (delay
                  (run-impl [{:path @iort}]
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
         (m/equals iort-diagnostics)
         (update-vals* @diagnostics count))))
    (it "sums correctly"
      (expect (= 34 (count (:diagnostics @results)))))
    (it "raises no errors"
      (expect (nil? (get diagnostics 'splint/error))))
    (it "raises no unknown errors"
      (expect (nil? (get diagnostics 'splint/unknown-error))))))
