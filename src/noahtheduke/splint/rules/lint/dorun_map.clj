; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.dorun-map
  (:require
    [noahtheduke.splint.rules :refer [defrule]]))

(defrule lint/dorun-map
  "`map` is lazy, which carries a performance and memory cost. `dorun` uses `seq` iteration to realize the entire sequence, returning `nil`. This style of iteration also carries a performance and memory cost. `dorun` is intended for more complex sequences, whereas a simple `map` can be accomplished with `reduce` + `conj`.

  `run!` uses `reduce` which non-lazy and has no `seq` overhead.

  Examples:

  ; bad
  (dorun (map println (range 10)))

  ; good
  (run! println (range 10))
  "
  {:pattern '(dorun (map ?fn ?coll))
   :message "Use `run!`, a non-lazy function."
   :replace '(run! ?fn ?coll)})
