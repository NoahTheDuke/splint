; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.apply-str
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn not-special? [form]
  (if (list? form)
    (not (#{'reverse 'interpose} (first form)))
    true))

(defrule style/apply-str
  "Check for round-about `clojure.string/join`.

  Examples:

  ; bad
  (apply str x)

  ; good
  (clojure.string/join x)
  "
  {:pattern '(apply str (? coll not-special?))
   :message "Use `clojure.string/join` instead of recreating it."
   :replace '(clojure.string/join ?coll)})
