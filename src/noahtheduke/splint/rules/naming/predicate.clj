; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.predicate
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn bad-name? [?name]
  (let [?name (str ?name)]
    (or (str/starts-with? ?name "is-")
      (str/ends-with? ?name "-p"))))

(defn good-name [?name]
  (let [?name (str ?name)]
    (cond
      (str/starts-with? ?name "is-") (str (subs ?name 3) (when-not (str/ends-with? ?name "?") "?"))
      (str/ends-with? ?name "-p") (str (subs ?name 0 (- (.length ?name) 2)) "?"))))

(defrule naming/predicate
  "Functions that return a boolean should end in a question mark.

  Doesn't verify the kind of function, just checks for anti-patterns in the
  names. Also doesn't actually check the classic Common Lisp convention as we
  have no way to know when a function name uses a word that naturally ends in
  a 'p' (such as `map`).

  Examples:

  ; bad
  (defn palindrome-p ...)
  (defn is-palindrome ...)

  ; good
  (defn palindrome? ...)
  "
  {:pattern '((? defn defn??) (? name bad-name?) ?*args)
   :message "Use a question mark instead of other language idioms."
   :on-match (fn [ctx rule form {:syms [?defn ?name ?args]}]
               (let [new-form (list* ?defn (symbol (good-name ?name)) ?args)]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
