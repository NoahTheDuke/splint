; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.let-when
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/let-when
  "`when-let` exists so use it.

  Examples:

  ; avoid
  (let [result (some-func)] (when result (do-stuff result)))

  ; prefer
  (when-let [result (some-func)] (do-stuff result))
  "
  {:patterns ['(let [?result ?given] (when ?result ?*args))
              '(let [?result ?given] (if ?result ?args))]
   :message "Use `when-let` instead of recreating it."
   :replace '(when-let [?result ?given] ?args)})
