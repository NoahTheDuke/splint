; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.dev.throws-on-match
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule dev/throws-on-match
  "Rules in `noahtheduke.splint` must be in sorted order."
  {:pattern '(very-special-symbol :do-not-match)
   :message "Deliberate throws if matched"
   :on-match (fn [ctx rule form _bindings]
               (throw (ex-info "matched" {:extra :data})))})
