; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.assoc-many
  (:require
   [noahtheduke.splint.clojure-ext.core :refer [mapv* ->list]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn even-pairs
  "Used in a thread-first macro."
  [ctx rule form ?pairs]
  (when-let [parent-form (:parent-form ctx)]
    (when (= '-> (first parent-form))
      (let [new-pairs (->> ?pairs
                        (partition 2)
                        (mapv* #(list 'assoc (first %) (second %))))
            new-form (reduce
                       (fn [acc cur]
                         (if (= form cur)
                           (into acc new-pairs)
                           (conj acc cur)))
                       []
                       parent-form)]
        (->diagnostic ctx rule parent-form
          {:replace-form (->list new-form)})))))

(defn odd-pairs
  "Used in a thread-first macro."
  [ctx rule form ?pairs]
  (let [new-form (apply
                   list '-> (nth ?pairs 0)
                   (->> (subvec ?pairs 1)
                     (partition 2)
                     (mapv* #(list 'assoc (first %) (second %)))))]
    (->diagnostic ctx rule form {:replace-form new-form})))

(defrule performance/assoc-many
  "Assoc takes multiple pairs but relies on `seq` stepping. This is slower than
  relying on multiple `assoc` invocations.

  Examples:

  ; avoid
  (assoc m :k1 1 :k2 2 :k3 3)

  ; prefer
  (-> m
      (assoc :k1 1)
      (assoc :k2 2)
      (assoc :k3 3))
  "
  {:pattern '(assoc ?*pairs)
   :message "Faster to call assoc multiple times."
   :on-match (fn [ctx rule form {:syms [?pairs]}]
               (let [cnt (count ?pairs)]
                 (if (even? cnt)
                   (when (< 2 cnt)
                     (even-pairs ctx rule form ?pairs))
                   (when (< 3 cnt)
                     (odd-pairs ctx rule form ?pairs)))))})
