; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.body-unquote-splicing
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn only-body [sexp]
  ('#{delay dosync future lazy-cat lazy-seq pvalues try with-loading-context} sexp))

(defn init-arg [sexp]
  ('#{binding locking sync with-bindings with-in-str with-local-vars
      with-precision with-redefs} sexp))

(defn only-body-new-form [?f ?body ?args]
  (list* ?f (list 'let ['res# (list 'do (list 'splint/unquote-splicing ?body))] 'res#) ?args))

(defn init-arg-new-form [?f ?init-arg ?body ?args]
  (list* ?f ?init-arg
    (list 'let ['res# (list 'do (list 'splint/unquote-splicing ?body))] 'res#)
    ?args))

(defrule lint/body-unquote-splicing
  "A macro that nests an `unquote-splicing` in a macro with a `& body` can lead to subtle hard to debug errors. Better to wrap the `unquote-splicing` in a `do` to force it into 'expression position'.

  @examples

  ; avoid
  `(binding [max mymax] ~@body)

  ; prefer
  `(binding [max mymax] (let [res# (do ~@body)] res#))
  "
  {:patterns ['((? f only-body) (splint/unquote-splicing (? body symbol?)) ?*args)
              '((? f init-arg) ?init-arg (splint/unquote-splicing (? body symbol?)) ?*args)]
   :message "Wrap ~@/unquote-splicing in `(let [res# (do ...)] res#)` to avoid unhygenic macro expansion."
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?f ?init-arg ?body ?args]}]
               (let [new-form (if ?init-arg
                                (init-arg-new-form ?f ?init-arg ?body ?args)
                                (only-body-new-form ?f ?body ?args))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
