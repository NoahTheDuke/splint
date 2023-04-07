; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.tostring
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule style/tostring
  "Convert `(.toString)` to `(str)`.

  Examples:

  ; bad
  (.toString x)

  ; good
  (str x)
  "
  {:pattern '(.toString ?x)
   :message "Use `str` instead of interop."
   :replace '(str ?x)})
