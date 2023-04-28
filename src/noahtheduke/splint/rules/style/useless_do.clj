; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.useless-do
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn not-unquote-splicing [sexp]
  (if (sequential? sexp)
    (not= 'splint/unquote-splicing (first sexp))
    true))

(defrule style/useless-do
  "A single item in a `do` is a no-op.

  Examples:

  ; bad
  (do coll)

  ; good
  coll"
  {:pattern '(do %not-unquote-splicing%-?x)
   :message "Unnecessary `do`."
   :replace '?x})
