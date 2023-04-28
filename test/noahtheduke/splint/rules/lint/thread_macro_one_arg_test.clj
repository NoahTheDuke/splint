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

(defexpect thread-style-inline-test
  (let [config '{lint/thread-macro-one-arg {:chosen-style :inline}}]
    (expect '(form arg) (check-alt "(-> arg form)" config))
    (expect '(form [arg]) (check-alt "(-> [arg] form)" config))
    (expect '(form {:a arg}) (check-alt "(-> {:a arg} form)" config))
    (expect '(form #{arg}) (check-alt "(-> #{arg} form)" config))
    (expect '(form arg) (check-alt "(->> arg form)" config))
    (expect '(form [arg]) (check-alt "(->> [arg] form)" config))
    (expect '(form {:a arg}) (check-alt "(->> {:a arg} form)" config))
    (expect '(form #{arg}) (check-alt "(->> #{arg} form)" config))))

(defexpect thread-style-avoid-collections-test
  (let [config '{lint/thread-macro-one-arg {:chosen-style :avoid-collections}}]
    (expect '(form arg) (check-alt "(-> arg form)" config))
    (expect nil? (check-alt "(-> [arg] form)" config))
    (expect nil? (check-alt "(-> {:a arg} form)" config))
    (expect nil? (check-alt "(-> #{arg} form)" config))
    (expect '(form arg) (check-alt "(->> arg form)" config))
    (expect nil? (check-alt "(->> [arg] form)" config))
    (expect nil? (check-alt "(->> {:a arg} form)" config))
    (expect nil? (check-alt "(->> #{arg} form)" config))))
