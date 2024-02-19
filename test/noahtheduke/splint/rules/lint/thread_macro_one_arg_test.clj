; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.thread-macro-one-arg-test
  (:require
    [clojure.test :refer [deftest]]
    [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [style]]
  (cond-> (single-rule-config 'lint/thread-macro-one-arg)
    style (assoc-in ['lint/thread-macro-one-arg :chosen-style] style)))

(deftest thread-first-1-arg-test
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg form)"
    (config))
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg (form))"
    (config))
  (expect-match
    '[{:alt (form arg 10)}]
    "(-> arg (form 10))"
    (config)))

(deftest thread-last-1-arg-test
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg form)"
    (config))
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg (form))"
    (config))
  (expect-match
    '[{:alt (form 10 arg)}]
    "(->> arg (form 10))"
    (config)))

(deftest thread-style-inline-test
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg form)"
    (config :inline))
  (expect-match
    '[{:alt (form [arg])}]
    "(-> [arg] form)"
    (config :inline))
  (expect-match
    '[{:alt (form {:a arg})}]
    "(-> {:a arg} form)"
    (config :inline))
  (expect-match
    '[{:alt (form #{arg})}]
    "(-> #{arg} form)"
    (config :inline))
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg form)"
    (config :inline))
  (expect-match
    '[{:alt (form [arg])}]
    "(->> [arg] form)"
    (config :inline))
  (expect-match
    '[{:alt (form {:a arg})}]
    "(->> {:a arg} form)"
    (config :inline))
  (expect-match
    '[{:alt (form #{arg})}]
    "(->> #{arg} form)"
    (config :inline)))

(deftest thread-style-avoid-collections-test
  (expect-match
    '[{:alt (form arg)}]
    "(-> arg form)"
    (config :avoid-collections))
  (expect-match nil
    "(-> [arg] form)"
    (config :avoid-collections))
  (expect-match nil
    "(-> {:a arg} form)"
    (config :avoid-collections))
  (expect-match nil
    "(-> #{arg} form)"
    (config :avoid-collections))
  (expect-match
    '[{:alt (form arg)}]
    "(->> arg form)"
    (config :avoid-collections))
  (expect-match nil
    "(->> [arg] form)"
    (config :avoid-collections))
  (expect-match nil
    "(->> {:a arg} form)"
    (config :avoid-collections))
  (expect-match nil
    "(->> #{arg} form)"
    (config :avoid-collections)))
