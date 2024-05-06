; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-vary-meta
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/prefer-vary-meta
  "`vary-meta` works like `swap!`, so no need to access and overwrite in two steps.

  Examples:

  ; avoid
  (with-meta x (assoc (meta x) :filename filename))

  ; prefer
  (vary-meta x assoc :filename filename)
  "
  {:pattern '(with-meta ?x (?f (meta ?x) ?*args))
   :message "Use `vary-meta` instead of recreating it."
   :replace '(vary-meta ?x ?f ?args)})
