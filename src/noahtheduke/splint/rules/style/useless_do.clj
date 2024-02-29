; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.useless-do
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [unquote-splicing??]]))

(set! *warn-on-reflection* true)

(defn not-unquote-splicing [sexp]
  (not (when (sequential? sexp) (unquote-splicing?? (first sexp)))))

(defrule style/useless-do
  "A single item in a `do` is a no-op. However, it is sometimes necessary to wrap expressions in `do`s to avoid issues, so `do` surrounding `~@something` will be skipped as well as `#(do something)`.

  Examples:

  ; bad
  (do coll)

  ; good
  coll

  ; skipped
  (do ~@body)
  #(do [%1 %2])
  "
  {:pattern '(do (? x not-unquote-splicing))
   :message "Unnecessary `do`."
   :on-match (fn [ctx rule form {:syms [?x]}]
               (let [parent-form (:parent-form ctx)]
                 (when-not (and (sequential? parent-form)
                             (= 'splint/fn (first parent-form)))
                   (->diagnostic ctx rule form {:replace-form ?x}))))})
