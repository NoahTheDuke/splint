(ns noahtheduke.spat.rules.thread-macro-one-arg 
  (:require
   [noahtheduke.spat.rules :refer [add-violation defrule]]
   [noahtheduke.spat.rules.helpers :refer [symbol-or-keyword-or-list?]]))

(defn thread-macro? [node]
  (#{'-> '->>} node))

(defrule thread-macro-one-arg 
  "Threading macros require more effort to understand so only use them with multiple
  args to help with readability.

  Examples:

  # bad
  (-> x y)
  (->> x y)

  # good
  (y x)

  # bad
  (-> x (y))
  (->> x (y))

  # good
  (y x)
  "
  {:pattern '(%thread-macro?%-?f ?arg ?form)
   :message "Intention is clearer with inlined form."
   :on-match (fn [ctx rule form {:syms [?f ?form ?arg]}]
               (when (symbol-or-keyword-or-list? ?form)
                 (let [replace-form (cond
                                      (not (list? ?form))
                                      (list ?form ?arg)
                                      (= '-> ?f)
                                      `(~(first ?form) ~?arg ~@(rest ?form))
                                      (= '->> ?f)
                                      (concat ?form [?arg]))]
                   (add-violation ctx rule form {:replace-form replace-form}))))})
