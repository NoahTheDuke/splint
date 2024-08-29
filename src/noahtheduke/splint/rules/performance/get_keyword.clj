; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.performance.get-keyword
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule performance/get-keyword
  "`clojure.core/get` is polymorphic and overkill if accessing a map with a keyword literal. The fastest is to fall the map itself as a function but that requires a `nil` check, so the safest fast method is to use the keyword as function.

  @examples

  ; avoid
  (get m :some-key)

  ; prefer
  (:some-key m)
  "
  {:pattern '(get ?m (? k keyword?))
   :message "Use keywords as functions instead of the polymorphic function `get`."
   :replace '(?k ?m)})
