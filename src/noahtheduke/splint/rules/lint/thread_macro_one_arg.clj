; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.thread-macro-one-arg
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(defn symbol-or-keyword-or-list? [sexp]
  (or (symbol? sexp)
      (keyword? sexp)
      (list? sexp)
      (and (sequential? sexp) (not (vector? sexp)))))

(defn thread-macro? [form]
  (case form
    (-> ->>) true
    false))

(defrule lint/thread-macro-one-arg
  "Threading macros require more effort to understand so only use them with multiple
  args to help with readability.

  Examples:

  ; bad
  (-> x y)
  (->> x y)

  ; good
  (y x)

  ; bad
  (-> x (y z))

  ; good
  (y x z)

  ; bad
  (->> x (y z))

  ; good
  (y z x)
  "
  {:pattern '(%thread-macro?%-?f ?arg ?form)
   :on-match (fn [ctx rule form {:syms [?f ?form ?arg]}]
               (when (symbol-or-keyword-or-list? ?form)
                 (let [replace-form (cond
                                      (not (list? ?form))
                                      (list ?form ?arg)
                                      (= '-> ?f)
                                      `(~(first ?form) ~?arg ~@(rest ?form))
                                      (= '->> ?f)
                                      (concat ?form [?arg]))
                       message (format "Intention of `%s` is clearer with inlined form."
                                       ?f)]
                   (->diagnostic rule form {:replace-form replace-form
                                            :message message}))))})
