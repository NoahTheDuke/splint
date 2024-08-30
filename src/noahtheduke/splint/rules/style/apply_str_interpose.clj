; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.apply-str-interpose
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/apply-str-interpose
  "Check for round-about `clojure.string/join`.

  @examples

  ; avoid
  (apply str (interpose \",\" x))

  ; prefer
  (clojure.string/join \",\" x)
  "
  {:pattern '(apply str (interpose ?x ?y))
   :message "Use `clojure.string/join` instead of recreating it."
   :autocorrect true
   :replace '(clojure.string/join ?x ?y)})
