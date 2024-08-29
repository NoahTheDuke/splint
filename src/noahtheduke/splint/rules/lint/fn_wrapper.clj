; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.fn-wrapper
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules.helpers :refer [default-import?]]))

(set! *warn-on-reflection* true)

(defn interop? [sexp]
  (and (symbol? sexp)
    (or (some->> sexp namespace symbol default-import?)
      (some->> sexp meta :splint/import-ns)
      (str/starts-with? (name sexp) "."))))

(defrule lint/fn-wrapper
  "Avoid wrapping functions in pass-through anonymous function defitions.

  @safety
  This rule is unsafe, as it can misunderstand when a function is or is not a method.

  @examples

  ; avoid
  (fn [num] (even? num))

  ; prefer
  even?

  ; avoid
  (let [f (fn [num] (even? num))] ...)

  ; prefer
  (let [f even?] ...)
  "
  {:patterns ['((? _ fn??) [?arg] (?fun ?arg))
              '((? _ fn??) ([?arg] (?fun ?arg)))]
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?fun ?args]}]
               (when-not (interop? ?fun)
                 (->diagnostic ctx rule form {:replace-form ?fun
                                              :message "No need to wrap function. Clojure supports first-class functions."})))})
