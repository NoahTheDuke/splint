; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns printer-test)

(when
  (not
    (= 1 1))
  (do
    (prn 2)
    (prn 3)))

(very-special-symbol :do-not-match)
