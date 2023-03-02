; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.rules.lint.update-in-one-arg
  (:require
    [noahtheduke.spat.rules :refer [defrule]]))

(defrule update-in-one-arg
  "`update-in` loops over the args, calling `update` for each key. If given a single key,
  just call `update` directly instead for performance and readability improvements.

  Examples:

  ; bad
  (update-in coll [:k] inc)
  (update-in coll [:k] + 1 2 3)

  ; good
  (update coll :k inc)
  (update coll :k + 1 2 3)
  "
  {:pattern '(update-in ?coll [?key] ?f &&. ?args)
   :message "Use `update` instead of recreating it."
   :replace '(update ?coll ?key ?f &&. ?args)})
