; This Source Code Form is subject to the terms of the Mozilla Publicsplin
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint
  "Entry-point for splint. Loads all of the lints up front, delegates all work
  to library functions."
  (:gen-class)
  (:require
    [noahtheduke.spat.pattern]
    [noahtheduke.splint.rules]
    [noahtheduke.splint.rules.helpers]
    [noahtheduke.splint.runner :as runner])
  (:require
    ;; # Rules
    ;; Please keep this sorted and split by genre

    noahtheduke.splint.rules.lint.apply-str
    noahtheduke.splint.rules.lint.apply-str-interpose
    noahtheduke.splint.rules.lint.apply-str-reverse
    noahtheduke.splint.rules.lint.assoc-assoc
    noahtheduke.splint.rules.lint.assoc-fn
    noahtheduke.splint.rules.lint.body-unquote-splicing
    noahtheduke.splint.rules.lint.conj-vector
    noahtheduke.splint.rules.lint.divide-by-one
    noahtheduke.splint.rules.lint.dorun-map
    noahtheduke.splint.rules.lint.dot-class-method
    noahtheduke.splint.rules.lint.dot-obj-method
    noahtheduke.splint.rules.lint.duplicate-field-name
    noahtheduke.splint.rules.lint.eq-false
    noahtheduke.splint.rules.lint.eq-nil
    noahtheduke.splint.rules.lint.eq-true
    noahtheduke.splint.rules.lint.eq-zero
    noahtheduke.splint.rules.lint.filter-complement
    noahtheduke.splint.rules.lint.filter-vec-filter
    noahtheduke.splint.rules.lint.first-first
    noahtheduke.splint.rules.lint.first-next
    noahtheduke.splint.rules.lint.fn-wrapper
    noahtheduke.splint.rules.lint.if-else-nil
    noahtheduke.splint.rules.lint.if-let-else-nil
    noahtheduke.splint.rules.lint.if-nil-else
    noahtheduke.splint.rules.lint.if-not-both
    noahtheduke.splint.rules.lint.if-not-do
    noahtheduke.splint.rules.lint.if-not-not
    noahtheduke.splint.rules.lint.if-same-truthy
    noahtheduke.splint.rules.lint.into-literal
    noahtheduke.splint.rules.lint.let-do
    noahtheduke.splint.rules.lint.let-if
    noahtheduke.splint.rules.lint.let-when
    noahtheduke.splint.rules.lint.loop-do
    noahtheduke.splint.rules.lint.loop-empty-when
    noahtheduke.splint.rules.lint.mapcat-apply-apply
    noahtheduke.splint.rules.lint.mapcat-concat-map
    noahtheduke.splint.rules.lint.minus-one
    noahtheduke.splint.rules.lint.minus-zero
    noahtheduke.splint.rules.lint.missing-body-in-when
    noahtheduke.splint.rules.lint.multiply-by-one
    noahtheduke.splint.rules.lint.multiply-by-zero
    noahtheduke.splint.rules.lint.neg-checks
    noahtheduke.splint.rules.lint.nested-addition
    noahtheduke.splint.rules.lint.nested-multiply
    noahtheduke.splint.rules.lint.next-first
    noahtheduke.splint.rules.lint.next-next
    noahtheduke.splint.rules.lint.not-empty
    noahtheduke.splint.rules.lint.not-eq
    noahtheduke.splint.rules.lint.not-nil
    noahtheduke.splint.rules.lint.not-some-pred
    noahtheduke.splint.rules.lint.plus-one
    noahtheduke.splint.rules.lint.plus-zero
    noahtheduke.splint.rules.lint.pos-checks
    noahtheduke.splint.rules.lint.redundant-call
    noahtheduke.splint.rules.lint.take-repeatedly
    noahtheduke.splint.rules.lint.thread-macro-one-arg
    noahtheduke.splint.rules.lint.tostring
    noahtheduke.splint.rules.lint.try-splicing
    noahtheduke.splint.rules.lint.update-in-assoc
    noahtheduke.splint.rules.lint.useless-do
    noahtheduke.splint.rules.lint.when-do
    noahtheduke.splint.rules.lint.when-not-call
    noahtheduke.splint.rules.lint.when-not-do
    noahtheduke.splint.rules.lint.when-not-empty
    noahtheduke.splint.rules.lint.when-not-not

    noahtheduke.splint.rules.naming.conversion-functions
    noahtheduke.splint.rules.naming.predicate
    noahtheduke.splint.rules.naming.record-name

    noahtheduke.splint.rules.style.cond-else
    noahtheduke.splint.rules.style.def-fn
    noahtheduke.splint.rules.style.new-object
    noahtheduke.splint.rules.style.prefer-boolean
    noahtheduke.splint.rules.style.prefer-clj-math
    noahtheduke.splint.rules.style.prefer-condp
    noahtheduke.splint.rules.style.prefer-vary-meta
    noahtheduke.splint.rules.style.redundant-let
    noahtheduke.splint.rules.style.set-literal-as-fn
    noahtheduke.splint.rules.style.single-key-in

    ,))

(set! *warn-on-reflection* true)

(defn -main
  "Pass-through to runner which does all the work."
  [& args]
  (runner/run args))
