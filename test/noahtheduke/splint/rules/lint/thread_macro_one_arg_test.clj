; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.thread-macro-one-arg-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(set! *warn-on-reflection* true)

(defexpect thread-first-1-arg-test
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg form)")
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg (form))")
  (expect-match
    '[{:alt (form arg 10)}]
    "(-> arg (form 10))"))

(defexpect thread-last-1-arg-test
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg form)")
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg (form))")
  (expect-match
    '[{:alt (form 10 arg)}]
    "(->> arg (form 10))"))

(defexpect thread-style-inline-test
  (let [config '{lint/thread-macro-one-arg {:chosen-style :inline}}]
    (expect-match
      '[{:alt (form arg)}]
      "(-> arg form)" config)
    (expect-match
      '[{:alt (form [arg])}]
      "(-> [arg] form)" config)
    (expect-match
      '[{:alt (form {:a arg})}]
      "(-> {:a arg} form)" config)
    (expect-match
      '[{:alt (form #{arg})}]
      "(-> #{arg} form)" config)
    (expect-match
      '[{:alt (form arg)}]
      "(->> arg form)" config)
    (expect-match
      '[{:alt (form [arg])}]
      "(->> [arg] form)" config)
    (expect-match
      '[{:alt (form {:a arg})}]
      "(->> {:a arg} form)" config)
    (expect-match
      '[{:alt (form #{arg})}]
      "(->> #{arg} form)" config)))

(defexpect thread-style-avoid-collections-test
  (let [config '{lint/thread-macro-one-arg {:chosen-style :avoid-collections}}]
    (expect-match
      '[{:alt (form arg)}]
      "(-> arg form)" config)
    (expect-match nil
      "(-> [arg] form)" config)
    (expect-match nil
      "(-> {:a arg} form)" config)
    (expect-match nil
      "(-> #{arg} form)" config)
    (expect-match
      '[{:alt (form arg)}]
      "(->> arg form)" config)
    (expect-match nil
      "(->> [arg] form)" config)
    (expect-match nil
      "(->> {:a arg} form)" config)
    (expect-match nil
      "(->> #{arg} form)" config)))
