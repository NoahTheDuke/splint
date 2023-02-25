(ns noahtheduke.spat.rules.dot-obj-method 
  (:require
   [noahtheduke.spat.rules :refer [add-violation defrule]]))

(defn symbol-not-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (not (if (neg? idx)
                (Character/isUpperCase ^char (first sym))
                (Character/isUpperCase ^char (nth sym (inc idx))))))))

(defrule dot-obj-method
  "Using the `.method` form maps the method call to Clojure's natural function position.

  # bad
  (. obj method args)

  # good
  (.method obj args)"
  {:pattern '(. ?obj %symbol?%-?method &&. ?args)
   :message "Intention is clearer with .method form."
   :on-match (fn [ctx rule form {:syms [?obj ?method ?args]}]
               (when (symbol-not-class? ?obj)
                 (let [replace-form `(~(symbol (str "." ?method)) ~?obj ~@?args)]
                   (add-violation ctx rule form {:replace-form replace-form}))))})
