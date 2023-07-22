; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.cond-else
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn not-else [form]
  (not= :else form))

(defn not-else-other [form]
  (and (not-else form)
       (or (keyword? form)
           (true? form))))

(defrule style/cond-else
  "It's nice when the default branch is consistent.

  Examples:

  ; bad
  (cond
    (< 10 num) (println 10)
    (< 5 num) (println 5)
    true (println 0))

  ; good
  (cond
    (< 10 num) (println 10)
    (< 5 num) (println 5)
    :else (println 0))
  "
  {:pattern2 '(cond (?+ pairs not-else) (? _ not-else-other) ?else)
   :message "Use `:else` as the catch-all branch."
   :replace '(cond ?pairs :else ?else)})
