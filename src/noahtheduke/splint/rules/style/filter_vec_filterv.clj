; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.filter-vec-filterv
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/filter-vec-filterv
  "filterv is preferable for using transients.

  Examples:

  ; avoid
  (vec (filter pred coll))

  ; prefer
  (filterv pred coll)
  "
  {:pattern '(vec (filter ?pred ?coll))
   :message "Use `filterv` instead of recreating it."
   :replace '(filterv ?pred ?coll)})
