; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.dot-equals
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/dot-equals
  "`=` is quite generalizable and built to handle immutable data. When using a literal, it can be significantly faster to use the underlying Java method.

  Currently only checks string literals.

  If `lint/prefer-method-values` is enabled, then the suggestion will use that syntax.

  Examples:

  ; bad
  (= \"foo\" s)

  ; good
  (.equals \"foo\" s)
  (String/equals \"foo\" s)
  "
  {:patterns ['(= (? string string?) ?any)
              '(= ?any (? string string?))]
   :ext :clj
   :on-match (fn [ctx rule form {:syms [?string ?any]}]
               (let [method-values (:enabled ('lint/prefer-method-values (:config ctx)))
                     replace-form (if method-values
                                    (list 'String/equals ?string ?any)
                                    (list '.equals ?string ?any))
                     msg (if method-values
                           "Rely on `String/equals` when comparing against string literals."
                           "Rely on `.equals` when comparing against string literals.")]
                 (->diagnostic ctx rule form {:replace-form replace-form
                                              :message msg})))})
