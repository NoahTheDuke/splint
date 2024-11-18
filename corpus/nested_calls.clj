; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

#_:splint/disable
(ns nested-calls)

(set! *warn-on-reflection* true)

(+ 1 2 (+ 3 4))

(str (some-call) (str (another-call)))

(str (str (another-call)))
