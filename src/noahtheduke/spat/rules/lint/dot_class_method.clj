; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

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
  "Using the `Obj/staticMethod` form maps the method call to Clojure's natural function
  position.

  # bad
  (. Obj staticMethod args)

  # good
  (Obj/staticMethod args)"
  {:pattern '(. ?class %symbol?%-?method &&. ?args)
   :message "Intention is clearer with `Obj/staticMethod` form."
   :on-match (fn [rule form {:syms [?class ?method ?args]}]
               (when (symbol-class? ?class)
                 (let [replace-form `(~(symbol (str ?class "/" ?method)) ~@?args)]
                   (->violation rule form {:replace-form replace-form}))))})
