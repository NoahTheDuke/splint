; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  "Entry-point for spat. Loads all of the lints up front, delegates all work to library
  functions."
  (:gen-class)
  (:require
    noahtheduke.spat.pattern
    noahtheduke.spat.rules
    [noahtheduke.spat.runner :as runner])
  (:require
    ;; # Rules
    ;; Please keep this sorted
    [noahtheduke.spat.rules.apply-str]
    [noahtheduke.spat.rules.apply-str-interpose]
    [noahtheduke.spat.rules.apply-str-reverse]
    [noahtheduke.spat.rules.assoc-assoc]
    [noahtheduke.spat.rules.assoc-fn]
    [noahtheduke.spat.rules.cond-else]
    [noahtheduke.spat.rules.conj-vector]
    [noahtheduke.spat.rules.divide-by-one]
    [noahtheduke.spat.rules.dorun-map]
    [noahtheduke.spat.rules.dot-class-method]
    [noahtheduke.spat.rules.dot-obj-method]
    [noahtheduke.spat.rules.eq-false]
    [noahtheduke.spat.rules.eq-nil]
    [noahtheduke.spat.rules.eq-zero]
    [noahtheduke.spat.rules.filter-complement]
    [noahtheduke.spat.rules.filter-fn-not-pred]
    [noahtheduke.spat.rules.filter-seq]
    [noahtheduke.spat.rules.filter-vec-filter]
    [noahtheduke.spat.rules.first-first]
    [noahtheduke.spat.rules.first-next]
    [noahtheduke.spat.rules.fn-wrapper]
    [noahtheduke.spat.rules.helpers]
    [noahtheduke.spat.rules.if-else-nil]
    [noahtheduke.spat.rules.if-let-else-nil]
    [noahtheduke.spat.rules.if-nil-else]
    [noahtheduke.spat.rules.if-not-both]
    [noahtheduke.spat.rules.if-not-do]
    [noahtheduke.spat.rules.if-not-not]
    [noahtheduke.spat.rules.if-same-truthy]
    [noahtheduke.spat.rules.if-then-do]
    [noahtheduke.spat.rules.into-literal]
    [noahtheduke.spat.rules.let-do]
    [noahtheduke.spat.rules.loop-do]
    [noahtheduke.spat.rules.loop-empty-when]
    [noahtheduke.spat.rules.mapcat-apply-apply]
    [noahtheduke.spat.rules.mapcat-concat-map]
    [noahtheduke.spat.rules.minus-one]
    [noahtheduke.spat.rules.minus-zero]
    [noahtheduke.spat.rules.multiply-by-one]
    [noahtheduke.spat.rules.multiply-by-zero]
    [noahtheduke.spat.rules.neg-checks]
    [noahtheduke.spat.rules.nested-addition]
    [noahtheduke.spat.rules.nested-multiply]
    [noahtheduke.spat.rules.next-first]
    [noahtheduke.spat.rules.next-next]
    [noahtheduke.spat.rules.not-empty]
    [noahtheduke.spat.rules.not-eq]
    [noahtheduke.spat.rules.not-nil]
    [noahtheduke.spat.rules.not-some-pred]
    [noahtheduke.spat.rules.plus-one]
    [noahtheduke.spat.rules.plus-zero]
    [noahtheduke.spat.rules.pos-checks]
    [noahtheduke.spat.rules.take-repeatedly]
    [noahtheduke.spat.rules.thread-macro-no-arg]
    [noahtheduke.spat.rules.thread-macro-one-arg]
    [noahtheduke.spat.rules.tostring]
    [noahtheduke.spat.rules.true-checks]
    [noahtheduke.spat.rules.update-in-assoc]
    [noahtheduke.spat.rules.useless-do]
    [noahtheduke.spat.rules.when-do]
    [noahtheduke.spat.rules.when-not-call]
    [noahtheduke.spat.rules.when-not-do]
    [noahtheduke.spat.rules.when-not-empty]
    [noahtheduke.spat.rules.when-not-not]
    [noahtheduke.spat.rules.with-meta]))

(set! *warn-on-reflection* true)

(defn -main
  "Pass-through to runner which does all the work."
  [& args]
  (runner/run args))
