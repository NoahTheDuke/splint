; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.dot-equals
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/dot-equals
  "`=` is quite generalizable and built to handle immutable data. When using a literal, it can be significantly faster to use the underlying Java method.

  Currently only checks string literals.

  Examples:

  # bad
  (= \"foo\" s)

  # good
  (.equals \"foo\" s)
  "
  {:patterns ['(= (? string string?) ?any)
              '(= ?any (? string string?))]
   :message "Rely on `.equals` when comparing against string literals."
   :ext :clj
   :replace '(.equals ?string ?any)})
