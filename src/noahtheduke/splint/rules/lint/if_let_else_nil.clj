; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.if-let-else-nil
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/if-let-else-nil
  "Idiomatic `if-let` defines both branches. `when-let` returns `nil` in the else branch.

  Examples:

  ; bad
  (if-let [a 1] a nil)

  ; good
  (when-let [a 1] a)
  "
  {:pattern '(if-let ?binding ?expr nil)
   :message "Use `when-let` instead of recreating it."
   :replace '(when-let ?binding ?expr)})
