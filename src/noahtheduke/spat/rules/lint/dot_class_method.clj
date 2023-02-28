(ns noahtheduke.spat.rules.lint.dot-class-method
  (:require
   [noahtheduke.spat.rules :refer [->violation defrule]]))

(defn symbol-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (if (neg? idx)
           (Character/isUpperCase ^char (first sym))
           (Character/isUpperCase ^char (nth sym (inc idx)))))))

(defrule dot-class-method
  "Using the `.method` form maps the method call to Clojure's natural function position.

  # bad
  (. Obj method args)

  # good
  (.method Obj args)"
  {:pattern '(. ?class %symbol?%-?method &&. ?args)
   :message "Intention is clearer with .method form."
   :on-match (fn [rule form {:syms [?class ?method ?args]}]
               (when (symbol-class? ?class)
                 (let [replace-form `(~(symbol (str ?class "/" ?method)) ~@?args)]
                   (->violation rule form {:replace-form replace-form}))))})
