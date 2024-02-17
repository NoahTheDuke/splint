; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.single-key-in
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(def multi->single
  '{assoc-in assoc
    get-in get
    update-in update})

(defrule style/single-key-in
  "`assoc-in` loops over the args, calling `assoc` for each key. If given a single key,
  just call `assoc` directly instead for performance and readability improvements.

  Examples:

  ; bad
  (assoc-in coll [:k] 10)

  ; good
  (assoc coll :k 10)
  "
  {:pattern '((? f multi->single) ?coll [?key] ?*vals)
   :on-match (fn [ctx rule form {:syms [?f ?coll ?key ?vals]}]
               (let [f (multi->single ?f)
                     new-form (list* f ?coll ?key ?vals)
                     message (format "Use `%s` instead of recreating it." f)]
                 (->diagnostic ctx rule form {:replace-form new-form
                                              :message message})))})
