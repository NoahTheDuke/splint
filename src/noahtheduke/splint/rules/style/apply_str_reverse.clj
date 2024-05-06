; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.apply-str-reverse
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/apply-str-reverse
  "Check for round-about `clojure.string/reverse`.

  Examples:

  ; avoid
  (apply str (reverse x))

  ; prefer
  (clojure.string/reverse x)
  "
  {:pattern '(apply str (reverse ?x))
   :message "Use `clojure.string/reverse` instead of recreating it."
   :replace '(clojure.string/reverse ?x)})
