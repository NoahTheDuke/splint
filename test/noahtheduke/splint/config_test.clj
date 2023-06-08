; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each in]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.config :as sut]))

(defexpect load-config-test
  (expect (match? {:parallel true :output "full"}
                  (sut/load-config nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "clj-kondo"}
                                   {:output "simple"})))
  (expect (match? {:parallel false}
                  (sut/load-config {'parallel false} nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "simple"} nil))))

(defexpect disable-single-rule-test
  (expect (match? {'style/plus-one {:enabled false}}
                  (sut/load-config {'style/plus-one {:enabled false}}
                                   nil))))

(defexpect disable-genre-test
  (let [config (update-vals @sut/default-config #(assoc % :enabled true))]
    (expect
      {:enabled false}
      (from-each [c (->> (sut/merge-config config {'style {:enabled false}})
                         (vals)
                         (filter #(= "style" (namespace (:rule-name % :a)))))]
        (in c)))
    (expect
      (match? {'style/plus-one {:enabled true}}
              (sut/merge-config config {'style {:enabled false}
                                        'style/plus-one {:enabled true}})))))
