; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.thread-macro-one-arg-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect thread-first-1-arg-test
  (expect '(form arg) (check-alt "(-> arg form)"))
  (expect '(form arg) (check-alt "(-> arg (form))"))
  (expect '(form arg 10) (check-alt "(-> arg (form 10))")))

(defexpect thread-last-1-arg-test
  (expect '(form arg) (check-alt "(->> arg form)"))
  (expect '(form arg) (check-alt "(->> arg (form))"))
  (expect '(form 10 arg) (check-alt "(->> arg (form 10))")))
