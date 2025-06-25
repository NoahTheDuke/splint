; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.update-with-swap
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.clojure-ext.core :refer [->list]]))

(set! *warn-on-reflection* true)

(defrule lint/update-with-swap
  "If an atom is stored inside if a map, the atom can be changed using `swap!` within an `update` or `update-in` call. However, `swap!` returns the new value, not the atom itself, so the container map will hold the deref'ed value of the atom, not the original atom. If the result of the `update` call is stored/used, this can lead to bugs.

  Additionally, if the return value of the `update` call is ignored, then the `update` form will work as expected (because the return value won't overwrite the existing map and the atom will be updated in place). This should be avoided as it breaks expectations about the value of values and normal behavior.

  @safety
  If the `update` call's return value isn't ignored (it's used in an assignment or passed to another call), switching to `swap!` will break the expected return value. Care must be exercised when switching.

  @examples

  ; avoid
  (update state :counter swap! + 5)

  ; prefer
  (swap! (:counter state) + 5)
  "
  {:pattern '((?| update [update update-in]) ?map ?key swap! ?+args)
   :autocorrect false
   :message "swap! in update derefs the value in the map."
   :on-match (fn [ctx rule form {:syms [?update ?map ?key ?args]}]
               (let [getter (if (= 'update ?update)
                              (list ?key ?map)
                              (list 'get-in ?map ?key))
                     new-form (->list (list* 'swap! getter ?args))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
