; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.conj-vector
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule conj-vector
  "`vector` is succinct and meaningful.

  Examples:

  ; bad
  (conj [] :a b {:c 1})

  ; good
  (vector :a b {:c 1})
  "
  {:pattern '(conj [] &&. ?x)
   :message "Use `vector` instead of recreating it."
   :replace '(vector &&. ?x)})
