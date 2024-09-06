; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.dot-class-method
  (:require
   [noahtheduke.splint.config :refer [get-config]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn method?? [sexp]
  (or (simple-symbol? sexp)
    (and (list? sexp)
      (= 1 (count sexp)))))

(defrule lint/dot-class-method
  "Using the `Obj/staticMethod` form maps the method call to Clojure's natural function position.

  @note
  This rule is disabled if `lint/prefer-method-values` is enabled to prevent conflicting diagnostics.

  @safety
  This rule is unsafe, as it can misunderstand when a symbol is or is not a class.

  @examples

  ; avoid
  (. Obj staticMethod args)
  (. Obj (staticMethod) args)

  ; prefer
  (Obj/staticMethod args)
  "
  {:pattern '(. (? class symbol-class?) (? method method??) ?*args)
   :message "Intention is clearer with `Obj/staticMethod` form."
   :on-match (fn [ctx rule form {:syms [?class ?method ?args] :as binds}]
               (when-not (:enabled (get-config ctx 'lint/prefer-method-values))
                 (let [?method (if (list? ?method) (first ?method) ?method)
                       replace-form `(~(symbol (str ?class "/" ?method)) ~@?args)]
                   (->diagnostic ctx rule form {:replace-form replace-form}))))})
