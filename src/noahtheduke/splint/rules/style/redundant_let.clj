; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.redundant-let
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/redundant-let
  "Directly nested lets can be merged into a single let block.

  Examples:

  ; bad
  (let [a 1]
    (let [b 2]
      (println a b)))

  (let [a 1
        b 2]
    (println a b))
  "
  {:pattern '(let (? outer-bindings vector?)
               (let (? inner-bindings vector?) ?*body))
   :message "Redundant let expressions can be merged."
   :on-match (fn [ctx rule form {:syms [?outer-bindings ?inner-bindings ?body]}]
               (let [new-form (list* 'let
                                     (vec (concat ?outer-bindings ?inner-bindings))
                                     ?body)]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
