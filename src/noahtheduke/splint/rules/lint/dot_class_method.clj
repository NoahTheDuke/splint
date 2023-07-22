; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.dot-class-method
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn symbol-class? [sym]
  (and (symbol? sym)
       (let [sym (pr-str sym)
             idx (.lastIndexOf sym ".")]
         (if (neg? idx)
           (Character/isUpperCase ^char (first sym))
           (Character/isUpperCase ^char (nth sym (inc idx)))))))

(defrule lint/dot-class-method
  "Using the `Obj/staticMethod` form maps the method call to Clojure's natural function
  position.

  Examples:

  ; bad
  (. Obj staticMethod args)

  ; good
  (Obj/staticMethod args)
  "
  {:pattern '(. (? class symbol-class?) ?method ?*args)
   :message "Intention is clearer with `Obj/staticMethod` form."
   :on-match (fn [ctx rule form {:syms [?class ?method ?args] :as binds}]
               (let [replace-form `(~(symbol (str ?class "/" ?method)) ~@?args)]
                 (->diagnostic ctx rule form {:replace-form replace-form})))})
