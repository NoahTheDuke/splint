(ns rules.dev.sorted-rules-require
  (:require
    [noahtheduke.splint.rules :refer [defrule ->violation]]))

(defrule sorted-rules-require
  "Rules in `noahtheduke.splint` must be in sorted order."
  {:pattern '(ns noahtheduke.splint &&. ?args)
   :message "Rules in `noahtheduke.splint` must be in sorted order."
   :on-match (fn [rule form {:syms [?args]}]
               (let [rules-require (->> ?args
                                        (filter #(and (seq? %) (= :require (first %))))
                                        (last))]
                 (when (not= (next rules-require) (sort (next rules-require)))
                   (->violation rule form))))})
