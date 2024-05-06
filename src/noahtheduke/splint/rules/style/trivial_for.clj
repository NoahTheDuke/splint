; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.trivial-for
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/trivial-for
  "`for` is a complex and weighty macro. When simply applying a function to each element, better to rely on other built-ins.

  Examples:

  ; avoid
  (for [item items]
    (f item))

  ; prefer
  (map f items)
  "
  {:pattern '(for [?item ?items] (?f ?item))
   :message "Avoid trivial usage of `for`."
   :replace '(map ?f ?items)})
