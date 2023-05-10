; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.metrics.fn-length
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]
    [noahtheduke.splint.rules.helpers :refer [parse-defn]]))

(set! *warn-on-reflection* true)

(defrule metrics/fn-length
  "Avoid functions longer than 10 lines of code."
  {:pattern '(%defn??%-?defn ?name &&. ?args)
   :on-match (fn [ctx rule form {:syms [?defn ?name ?args]}]
               (when-let [defn-form (parse-defn ?name ?args)]
                 (keep
                   (fn [fn-body]
                     (when-let [m (meta fn-body)]
                       (let [len (- (:end-row m 0) (:line m 0))]
                         (when (< 10 len)
                           (->diagnostic rule fn-body {:message "Function bodies shouldn't be longer than 10 lines."})))))
                   (:arities defn-form))))})
