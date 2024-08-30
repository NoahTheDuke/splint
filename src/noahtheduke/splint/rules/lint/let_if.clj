; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.let-if
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/let-if
  "`if-let` exists so use it.
  
  @safety
  Suggestions can be wrong as there's no code-walking to determine if `result` binding is used in falsy branch.

  @examples

  ; avoid
  (let [result (some-func)] (if result (do-stuff result) (other-stuff)))

  ; prefer
  (if-let [result (some-func)] (do-stuff result) (other-stuff))
  "
  {:pattern '(let [?result ?given] (if ?result ?truthy ?falsy))
   :message "Use `if-let` instead of recreating it."
   :autocorrect true
   :replace '(if-let [?result ?given] ?truthy ?falsy)})
