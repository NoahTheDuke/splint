; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.style.single-key-in
  (:require
    [noahtheduke.spat.rules :refer [defrule ->violation]]))

(defn getter [sexp]
  (#{'assoc-in 'get-in 'update-in} sexp))

(defn setter [?f]
  (case ?f
    assoc-in 'assoc
    get-in 'get
    update-in 'update))

(defrule single-key-in
  "`assoc-in` loops over the args, calling `assoc` for each key. If given a single key,
  just call `assoc` directly instead for performance and readability improvements.

  Examples:

  ; bad
  (assoc-in coll [:k] 10)

  ; good
  (assoc coll :k 10)
  "
  {:pattern '(%getter%-?f ?coll [?key] &&. ?vals)
   :on-match (fn [rule form {:syms [?f ?coll ?key ?vals]}]
               (let [new-form (list* (setter ?f) ?coll ?key ?vals)
                     message (format "Use `%s` instead of recreating it." (setter ?f))]
                 (->violation rule form {:replace-form new-form
                                         :message message})))})