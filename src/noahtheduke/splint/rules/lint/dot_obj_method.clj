; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.dot-obj-method
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn symbol-not-class? [sym]
  (and (symbol? sym)
    (let [sym (pr-str sym)
          idx (.lastIndexOf sym ".")]
      (not (if (neg? idx)
             (Character/isUpperCase ^char (first sym))
             (Character/isUpperCase ^char (nth sym (inc idx))))))))

(defrule lint/dot-obj-method
  "Using the `.method` form maps the method call to Clojure's natural function position.

  Examples:

  ; bad
  (. obj method args)

  ; good
  (.method obj args)
  "
  {:pattern '(. ?obj (? method symbol-not-class?) ?*args)
   :message "Intention is clearer with `.method` form."
   :on-match (fn [ctx rule form {:syms [?obj ?method ?args]}]
               (let [replace-form `(~(symbol (str "." ?method)) ~?obj ~@?args)]
                 (->diagnostic ctx rule form {:replace-form replace-form})))})
