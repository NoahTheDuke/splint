; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.try-splicing
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/try-splicing
  "A macro that wraps a splicing unquote in a try-catch or try-finally can lead
  to subtle hard to debug errors. Better to wrap the splicing unquote in a `do`
  to force it into 'expression position'.

  @examples

  ; avoid
  `(try ~@body (finally :true))

  ; prefer
  `(try (do ~@body) (finally :true))
  "
  {:pattern '(try (splint/unquote-splicing ?body) ?*args)
   :message "Wrap splicing unquotes in a `try` in a `do` to catch subtle bugs."
   :autocorrect true
   :replace '(try (do (splint/unquote-splicing ?body)) ?args)})
