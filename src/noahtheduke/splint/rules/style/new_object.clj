; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.new-object
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/new-object
  "`new` is discouraged for dot usage.

  Examples:

  ; bad
  (new java.util.ArrayList 100)

  ; good
  (java.util.ArrayList. 100)
  "
  {:pattern '(new ?class &&. ?args)
   :message "dot creation is preferred."
   :on-match (fn [ctx rule form {:syms [?class ?args]}]
               (let [class-dot (symbol (str ?class "."))
                     new-form `(~class-dot ~@?args)]
                 (->diagnostic rule form {:replace-form new-form})))})
