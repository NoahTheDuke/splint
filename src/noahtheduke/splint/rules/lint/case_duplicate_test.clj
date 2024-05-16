; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.case-duplicate-test
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.printer :refer [pprint-str]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn check-test-case
  [state tc]
  (if (contains? (:acc state) tc)
    (update state :doubles conj tc)
    (update state :acc conj tc)))

(defn build-diagnostics [ctx rule form ?clauses]
  (let [?clauses (if (odd? (count ?clauses))
                   (butlast ?clauses)
                   ?clauses)
        tests (reduce
                (fn reduce-fn [state tc]
                  (if (list? tc)
                    (reduce check-test-case state tc)
                    (check-test-case state tc)))
                {:acc #{}
                 :doubles []}
                (take-nth 2 ?clauses))]
    (for [double (:doubles tests)
          :let [msg (str "Duplicate case test constant: "
                      (-> (pprint-str double)
                        (str/replace "splint/" "")))]]
      (->diagnostic ctx rule form {:form-meta (meta double)
                                   :message msg}))))

(defrule lint/case-duplicate-test
  "It's an error to have duplicate `case` test constants.

  Examples:

  ; avoid
  (case x :foo :bar :foo :baz)
  "
  {:pattern '(case ?expr ?*clauses)
   :on-match (fn [ctx rule form {:syms [?expr ?clauses]}]
               (build-diagnostics ctx rule form ?clauses))})
