; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.style.prefer-vary-meta
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule prefer-vary-meta
  "`vary-meta` works like swap!, so no need to access and overwrite in two steps.

  Examples:

  ; bad
  (with-meta x (assoc (meta x) :filename filename))

  ; good
  (vary-meta x assoc :filename filename)
  "
  {:pattern '(with-meta ?x (?f (meta ?x) &&. ?args))
   :message "Use `vary-meta` instead of recreating it."
   :replace '(vary-meta ?x ?f &&. ?args)})
