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
    (:splint/import-ns (meta sym))))

(defn method?? [sexp]
  (or (symbol? sexp)
    (and (list? sexp)
      (= 1 (count sexp)))))

(defrule lint/dot-class-method
  "Using the `Obj/staticMethod` form maps the method call to Clojure's natural function
  position.

  Examples:

  ; avoid
  (. Obj staticMethod args)
  (. Obj (staticMethod) args)

  ; prefer
  (Obj/staticMethod args)
  "
  {:pattern '(. (? class symbol-class?) (? method method??) ?*args)
   :message "Intention is clearer with `Obj/staticMethod` form."
   :on-match (fn [ctx rule form {:syms [?class ?method ?args] :as binds}]
               (let [?method (if (list? ?method) (first ?method) ?method)
                     replace-form `(~(symbol (str ?class "/" ?method)) ~@?args)]
                 (->diagnostic ctx rule form {:replace-form replace-form})))})
