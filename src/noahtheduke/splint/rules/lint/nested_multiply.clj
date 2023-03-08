; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.nested-multiply
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defn *? [sexp]
  (#{'* '*'} sexp))

(defrule lint/nested-multiply
  "Checks for simple nested multiply.

  Examples:

  ; bad
  (* x (* y z))
  (* x (* y z a))

  ; good
  (* x y z)
  (* x y z a)
  "
  {:pattern '(%*?%-?p ?x (?p &&. ?xs))
   :message "Use the variadic arity of `*`."
   :replace '(?p ?x &&. ?xs)})
