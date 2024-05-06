; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.nested-addition
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn +? [form]
  (case form
    (+ +') true
    false))

(defrule style/nested-addition
  "Checks for simple nested additions.

  Examples:

  ; avoid
  (+ x (+ y z))
  (+ x (+ y z a))

  ; prefer
  (+ x y z)
  (+ x y z a)
  "
  {:pattern '((? p +?) ?x (?p ?*xs))
   :message "Use the variadic arity of `+`."
   :replace '(?p ?x ?xs)})
