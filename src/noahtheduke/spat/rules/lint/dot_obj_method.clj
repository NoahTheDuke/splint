; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.dot-obj-method
  (:require
   [noahtheduke.spat.rules :refer [->violation defrule]]))

(defn symbol-not-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (not (if (neg? idx)
                (Character/isUpperCase ^char (first sym))
                (Character/isUpperCase ^char (nth sym (inc idx))))))))

(defrule dot-obj-method
  "Using the `.method` form maps the method call to Clojure's natural function position.

  Examples:

  ; bad
  (. obj method args)

  ; good
  (.method obj args)
  "
  {:pattern '(. ?obj %symbol?%-?method &&. ?args)
   :message "Intention is clearer with `.method` form."
   :on-match (fn [rule form {:syms [?obj ?method ?args]}]
               (when (symbol-not-class? ?obj)
                 (let [replace-form `(~(symbol (str "." ?method)) ~?obj ~@?args)]
                   (->violation rule form {:replace-form replace-form}))))})
