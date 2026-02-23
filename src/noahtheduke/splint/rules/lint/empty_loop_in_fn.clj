; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.empty-loop-in-fn
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [defn-fn??]]))

(set! *warn-on-reflection* true)

(defrule lint/empty-loop-in-fn
  "A function is a valid `recur` target, allowing for writing recursive functions directly. No need to nest within a `loop` if both function and loop have no parameters.

  @note
  Autocorrect isn't enabled because of formatting and comment changes.

  @examples

  ; avoid
  (defn example []
    (loop []
      (when (= 2 (+ 1 1))
        (recur))))

  ; prefer
  (defn example []
    (when (= 2 (+ 1 1))
      (recur)))
  "
  {:patterns ['((? ?def-sym defn-fn??) (?? ?name symbol?) ?*args
                 [] (?? ?prepost-map? map?)
                 (loop []
                   ?*body))
              '((? ?def-sym defn-fn??) (?? ?name2 symbol?) ?*args
                 ([] (?? ?prepost-map? map?)
                   (loop []
                     ?*body))
                 (?? ?attr-map map?))]
   :message "Empty loop can be removed for direct recursion"
   :on-match (fn [ctx rule form {:syms [?def-sym ?name ?name2 ?args ?prepost-map ?body ?attr-map]
                                 :as matches}]
               (let [new-form (if (seq ?name)
                                `(~?def-sym ~@?name ~@?args [] ~@?prepost-map ~@?body)
                                `(~?def-sym ~@?name2 ~@?args ([] ~@?prepost-map ~@?body) ~@?attr-map))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
