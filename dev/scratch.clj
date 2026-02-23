; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

#_{:splint/disable [naming/single-segment-namespace]}
(ns scratch
  (:require
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [noahtheduke.splint.config :refer [read-default-config]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule global-rules]]
   [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(defrule dev/find-specific-shape
  "Rules in `noahtheduke.splint` must be in sorted order."
  {:patterns ['(defn ?name ?*args
                 [] (?? ?prepost-map? map?)
                 (loop []
                   ?*body))
              '(defn ?name2 ?*args
                 ([] (?? ?prepost-map? map?)
                   (loop []
                     ?*body))
                 (?? ?attr-map map?))]
   :message "Deliberate throws if matched"
   :on-match (fn [ctx rule form _bindings]
               (->diagnostic ctx rule form {:replace-form nil}))})

(def results
  (delay
    (time
      (run-impl [{:path "/Users/noah/programming/open-source/ts-clojure/clojars-samples/data/clojars-repos"}]
        {:config-override
         (-> (read-default-config)
           (update-vals* #(assoc % :enabled true))
           (assoc :silent true)
           (assoc :parallel true)
           #_(assoc :autocorrect true)
           (assoc :clojure-version {:major 1 :minor 12}))}))))

(def results-2
  (delay
    (time
      (run-impl [{:path "/Users/noah/programming/open-source/ts-clojure/clojars-samples/data/clojars-repos"}]
        {:config-override
         (-> (read-default-config)
           (update-vals* #(assoc % :enabled false))
           (assoc-in ['dev/find-specific-shape :enabled] true)
           (assoc :silent true)
           (assoc :parallel true)
           #_(assoc :autocorrect true)
           (assoc :clojure-version {:major 1 :minor 12}))}))))

(def blank-rules
  (into (sorted-map) (update-vals (:rules @global-rules) (fn [_] 0))))

(def diagnostics
  (delay (->> @results
           :diagnostics
           (group-by :rule-name))))

(def diagnostic-counts
  (delay (->> @results
           :diagnostics
           (reduce
             (fn [m v]
               (update m (:rule-name v) (fnil inc 0)))
             blank-rules))))

(def diagnostics-2
  (delay (->> @results-2
           :diagnostics
           (group-by :rule-name))))

(comment
  (sort-by val @diagnostic-counts)
  (get @diagnostics 'style/minus-zero)
  (count (get @diagnostics-2 'dev/find-specific-shape))
  (:diagnostics @results-2))
