; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.single-literal-merge
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn select-style [ctx rule]
  (if (:enabled ('performance/assoc-many (:config ctx)))
    :multiple
    (:chosen-style (:config rule))))

(defn single-assoc [?given ?literal]
  (list* 'assoc ?given
         (->> ?literal
              (sort-by key)
              (mapcat identity))))

(defn multi-assoc [?given ?literal]
  (list* '-> ?given
         (->> ?literal
              (sort-by key)
              (mapv (fn [[k v]] (list 'assoc k v))))))

(defrule performance/single-literal-merge
  "`clojure.core/merge` is inherently slow. Its major benefit is handling nil
  values. If there is only a single object to merge in and it's a map literal,
  that benefit is doubly unused. Better to directly assoc the values in.

  NOTE: If the chosen style is `:single` and `performance/assoc-many` is
  enabled, the style will be treated as `:multiple` to make the warnings
  consistent.

  Examples:

  ; bad
  (merge m {:a 1 :b 2 :c 3})

  ; good
  (assoc m :a 1 :b 2 :c 3)
  "
  {:pattern '(merge ?given (? literal map?))
   :message "Prefer assoc for merging literal maps"
   :on-match (fn [ctx rule form {:syms [?given ?literal]}]
               (when-let [?literal (not-empty ?literal)]
                 (let [new-form (condp = (select-style ctx rule)
                                  :single (single-assoc ?given ?literal)
                                  :multiple (multi-assoc ?given ?literal))]
                   (->diagnostic ctx rule form {:replace-form new-form}))))})
