; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.apply-str
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defn not-special? [form]
  (if (list? form)
    (not (#{'reverse 'interpose} (first form)))
    true))

(defrule apply-str
  "Check for round-about clojure.string/reverse.

  Examples:

  ; bad
  (apply str x)

  ; good
  (clojure.string/join x)
  "
  {:pattern '(apply str %not-special?%-?coll)
   :message "Use `clojure.string/join` instead of recreating it."
   :replace '(clojure.string/join ?coll)})
