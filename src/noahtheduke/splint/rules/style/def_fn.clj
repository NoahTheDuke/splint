; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.def-fn
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/def-fn
  "`(defn [])` is preferable over `(def (fn []))`. Extrapolate to closures.

  Examples:

  ; avoid
  (def check-inclusion
    (let [allowed #{:a :b :c}]
      (fn [i] (contains? allowed i))))

  ; prefer
  (let [allowed #{:a :b :c}]
    (defn check-inclusion [i]
      (contains? allowed i)))

  ; avoid
  (def some-func
    (fn [i] (+ i 100)))

  ; prefer
  (defn some-func [i]
    (+ i 100))
  "
  {:patterns ['(def ?name ?*args ((? fn fn??) ?*fn-body))
              '(def ?name ?*args (let ?binds ((? fn fn??) ?*fn-body)))]
   :on-match (fn [ctx rule form {:syms [?name ?args ?binds ?fn-body]}]
               (let [new-form (if ?binds
                                (list 'let ?binds (list* 'defn ?name (concat ?args ?fn-body)))
                                (list* 'defn ?name (concat ?args ?fn-body)))
                     message (if ?binds
                               "Prefer `let` wrapping `defn`."
                               "Prefer `defn` instead of `def` wrapping `fn`.")]
                 (->diagnostic ctx rule form {:replace-form new-form
                                              :message message})))})
