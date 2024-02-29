; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-boolean
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/prefer-boolean
  "Use `boolean` if you must return `true` or `false` from an expression.

  Examples:

  ; bad
  (if some-val true false)
  (if (some-func) true false)

  ; good
  (boolean some-val)
  (boolean (some-func))"
  {:pattern '(if ?test-expr true false)
   :message "Use `boolean` if you must return `true` or `false`."
   :replace '(boolean ?test-expr)})
