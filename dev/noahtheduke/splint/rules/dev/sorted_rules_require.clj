; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.dev.sorted-rules-require
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule dev/sorted-rules-require
  "Rules in `noahtheduke.splint` must be in sorted order."
  {:pattern '(ns noahtheduke.splint ?*args)
   :message "Rules in `noahtheduke.splint` must be in sorted order."
   :on-match (fn [ctx rule form {:syms [?args]}]
               (let [rules-require (->> ?args
                                        (filter #(and (seq? %) (= :require (first %))))
                                        (last))]
                 (when-not (= (next rules-require) (sort (next rules-require)))
                   (->diagnostic ctx rule form))))})
