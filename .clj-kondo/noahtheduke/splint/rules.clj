(ns noahtheduke.splint.rules
  (:require
    [clj-kondo.hooks-api :as api]))

(defn defrule
  [{:keys [node]}]
  (let [[rule-name & body] (next (:children node))
        node (api/list-node (list* (api/token-node 'def)
                                 (api/token-node (symbol (name (api/sexpr rule-name))))
                                 body))]
    {:node node}))
