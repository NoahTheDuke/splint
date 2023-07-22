; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.conj-vector
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/conj-vector
  "`vector` is succinct and meaningful.

  Examples:

  ; bad
  (conj [] :a b {:c 1})

  ; good
  (vector :a b {:c 1})
  "
  {:pattern2 '(conj [] ?+x)
   :message "Use `vector` instead of recreating it."
   :replace '(vector ?x)})
