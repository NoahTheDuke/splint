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
    ;; Please keep this sorted and split by genre

    [noahtheduke.spat.rules.lint.apply-str]
    [noahtheduke.spat.rules.lint.apply-str-interpose]
    [noahtheduke.spat.rules.lint.apply-str-reverse]
    [noahtheduke.spat.rules.lint.assoc-assoc]
    [noahtheduke.spat.rules.lint.assoc-fn]
    [noahtheduke.spat.rules.lint.assoc-in-one-arg]
    [noahtheduke.spat.rules.lint.cond-else]
    [noahtheduke.spat.rules.lint.conj-vector]
    [noahtheduke.spat.rules.lint.divide-by-one]
    [noahtheduke.spat.rules.lint.dorun-map]
    [noahtheduke.spat.rules.lint.dot-class-method]
    [noahtheduke.spat.rules.lint.dot-obj-method]
    [noahtheduke.spat.rules.lint.eq-false]
    [noahtheduke.spat.rules.lint.eq-nil]
    [noahtheduke.spat.rules.lint.eq-true]
    [noahtheduke.spat.rules.lint.eq-zero]
    [noahtheduke.spat.rules.lint.filter-complement]
    [noahtheduke.spat.rules.lint.filter-vec-filter]
    [noahtheduke.spat.rules.lint.first-first]
    [noahtheduke.spat.rules.lint.first-next]
    [noahtheduke.spat.rules.lint.fn-wrapper]
    [noahtheduke.spat.rules.lint.if-else-nil]
    [noahtheduke.spat.rules.lint.if-let-else-nil]
    [noahtheduke.spat.rules.lint.if-nil-else]
    [noahtheduke.spat.rules.lint.if-not-both]
    [noahtheduke.spat.rules.lint.if-not-do]
    [noahtheduke.spat.rules.lint.if-not-not]
    [noahtheduke.spat.rules.lint.if-same-truthy]
    [noahtheduke.spat.rules.lint.if-then-do]
    [noahtheduke.spat.rules.lint.into-literal]
    [noahtheduke.spat.rules.lint.let-do]
    [noahtheduke.spat.rules.lint.let-if]
    [noahtheduke.spat.rules.lint.loop-do]
    [noahtheduke.spat.rules.lint.let-when]
    [noahtheduke.spat.rules.lint.loop-empty-when]
    [noahtheduke.spat.rules.lint.mapcat-apply-apply]
    [noahtheduke.spat.rules.lint.mapcat-concat-map]
    [noahtheduke.spat.rules.lint.minus-one]
    [noahtheduke.spat.rules.lint.minus-zero]
    [noahtheduke.spat.rules.lint.missing-body-in-when]
    [noahtheduke.spat.rules.lint.multiply-by-one]
    [noahtheduke.spat.rules.lint.multiply-by-zero]
    [noahtheduke.spat.rules.lint.neg-checks]
    [noahtheduke.spat.rules.lint.nested-addition]
    [noahtheduke.spat.rules.lint.nested-multiply]
    [noahtheduke.spat.rules.lint.next-first]
    [noahtheduke.spat.rules.lint.next-next]
    [noahtheduke.spat.rules.lint.not-empty]
    [noahtheduke.spat.rules.lint.not-eq]
    [noahtheduke.spat.rules.lint.not-nil]
    [noahtheduke.spat.rules.lint.not-some-pred]
    [noahtheduke.spat.rules.lint.plus-one]
    [noahtheduke.spat.rules.lint.plus-zero]
    [noahtheduke.spat.rules.lint.pos-checks]
    [noahtheduke.spat.rules.lint.take-repeatedly]
    [noahtheduke.spat.rules.lint.thread-macro-no-arg]
    [noahtheduke.spat.rules.lint.thread-macro-one-arg]
    [noahtheduke.spat.rules.lint.tostring]
    [noahtheduke.spat.rules.lint.update-in-assoc]
    [noahtheduke.spat.rules.lint.update-in-one-arg]
    [noahtheduke.spat.rules.lint.useless-do]
    [noahtheduke.spat.rules.lint.when-do]
    [noahtheduke.spat.rules.lint.when-not-call]
    [noahtheduke.spat.rules.lint.when-not-do]
    [noahtheduke.spat.rules.lint.when-not-empty]
    [noahtheduke.spat.rules.lint.when-not-not]
    [noahtheduke.spat.rules.lint.with-meta-vary-meta]

    [noahtheduke.spat.rules.style.new-object] 

    ))

(set! *warn-on-reflection* true)

(defn -main
  "Pass-through to runner which does all the work."
  [& args]
  (runner/run args))
