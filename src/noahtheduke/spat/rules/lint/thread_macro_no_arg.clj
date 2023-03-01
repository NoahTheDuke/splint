; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.thread-macro-no-arg
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defn thread-macro? [node]
  (#{'-> '->>} node))

(defrule thread-macro-no-arg
  "Avoid wrapping vars in a threading macro.

  Examples:

  # bad
  (-> x)
  (->> x)

  # good
  x
  "
  {:pattern '(%thread-macro? ?x)
   :message "Single-arg threading macros are a no-op."
   :replace '?x})
