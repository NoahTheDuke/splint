; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.let-if
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule let-if
  "`if-let` exists so use it. Suggestions can be wrong as there's no code-walking to
  determine if `result` binding is used in falsy branch.

  Examples:

  ; bad
  (let [result (some-func)] (if result (do-stuff result) (other-stuff)))

  ; good
  (if-let [result (some-func)] (do-stuff result) (other-stuff))
  "
  {:pattern '(let [?result ?given] (if ?result ?truthy ?falsy))
   :message "Use `if-let` instead of recreating it."
   :replace '(if-let [?result ?given] ?truthy ?falsy)})
