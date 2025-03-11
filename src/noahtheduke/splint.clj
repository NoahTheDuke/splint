; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint
  "Entry-point for splint. Loads all of the lints up front, delegates all work
  to library functions."
  (:gen-class)
  (:require
   [noahtheduke.splint.runners.autocorrect] 
   [noahtheduke.splint.path-matcher]
   [noahtheduke.splint.pattern]
   [noahtheduke.splint.rules.helpers]
   [noahtheduke.splint.rules]
   [noahtheduke.splint.runner :as runner])
  (:require
    ;; # Rules
    ;; Please keep this sorted and split by genre

   noahtheduke.splint.rules.lint.assoc-fn
   noahtheduke.splint.rules.lint.body-unquote-splicing
   noahtheduke.splint.rules.lint.defmethod-names
   noahtheduke.splint.rules.lint.divide-by-one
   noahtheduke.splint.rules.lint.dorun-map
   noahtheduke.splint.rules.lint.dot-class-method
   noahtheduke.splint.rules.lint.dot-obj-method
   noahtheduke.splint.rules.lint.duplicate-case-test
   noahtheduke.splint.rules.lint.duplicate-field-name
   noahtheduke.splint.rules.lint.fn-wrapper
   noahtheduke.splint.rules.lint.if-else-nil
   noahtheduke.splint.rules.lint.if-let-else-nil
   noahtheduke.splint.rules.lint.if-nil-else
   noahtheduke.splint.rules.lint.if-not-both
   noahtheduke.splint.rules.lint.if-not-do
   noahtheduke.splint.rules.lint.if-not-not
   noahtheduke.splint.rules.lint.if-same-truthy
   noahtheduke.splint.rules.lint.into-literal
   noahtheduke.splint.rules.lint.let-if
   noahtheduke.splint.rules.lint.let-when
   noahtheduke.splint.rules.lint.locking-object
   noahtheduke.splint.rules.lint.loop-do
   noahtheduke.splint.rules.lint.loop-empty-when
   noahtheduke.splint.rules.lint.misplaced-type-hint
   noahtheduke.splint.rules.lint.missing-body-in-when
   noahtheduke.splint.rules.lint.not-empty
   noahtheduke.splint.rules.lint.prefer-method-values
   noahtheduke.splint.rules.lint.prefer-require-over-use
   noahtheduke.splint.rules.lint.redundant-call
   noahtheduke.splint.rules.lint.redundant-str-call
   noahtheduke.splint.rules.lint.require-explicit-param-tags
   noahtheduke.splint.rules.lint.take-repeatedly
   noahtheduke.splint.rules.lint.thread-macro-one-arg
   noahtheduke.splint.rules.lint.try-splicing
   noahtheduke.splint.rules.lint.underscore-in-namespace
   noahtheduke.splint.rules.lint.warn-on-reflection

   noahtheduke.splint.rules.metrics.fn-length
   noahtheduke.splint.rules.metrics.parameter-count

   noahtheduke.splint.rules.naming.conventional-aliases
   noahtheduke.splint.rules.naming.conversion-functions
   noahtheduke.splint.rules.naming.lisp-case
   noahtheduke.splint.rules.naming.predicate
   noahtheduke.splint.rules.naming.record-name
   noahtheduke.splint.rules.naming.single-segment-namespace

   noahtheduke.splint.rules.performance.assoc-many
   noahtheduke.splint.rules.performance.avoid-satisfies
   noahtheduke.splint.rules.performance.dot-equals
   noahtheduke.splint.rules.performance.get-in-literals
   noahtheduke.splint.rules.performance.get-keyword
   noahtheduke.splint.rules.performance.into-transducer
   noahtheduke.splint.rules.performance.single-literal-merge

   noahtheduke.splint.rules.style.apply-str
   noahtheduke.splint.rules.style.apply-str-interpose
   noahtheduke.splint.rules.style.apply-str-reverse
   noahtheduke.splint.rules.style.assoc-assoc
   noahtheduke.splint.rules.style.cond-else
   noahtheduke.splint.rules.style.conj-vector
   noahtheduke.splint.rules.style.def-fn
   noahtheduke.splint.rules.style.eq-false
   noahtheduke.splint.rules.style.eq-nil
   noahtheduke.splint.rules.style.eq-true
   noahtheduke.splint.rules.style.eq-zero
   noahtheduke.splint.rules.style.filter-complement
   noahtheduke.splint.rules.style.filter-vec-filterv
   noahtheduke.splint.rules.style.first-first
   noahtheduke.splint.rules.style.first-next
   noahtheduke.splint.rules.style.is-eq-order
   noahtheduke.splint.rules.style.let-do
   noahtheduke.splint.rules.style.mapcat-apply-apply
   noahtheduke.splint.rules.style.mapcat-concat-map
   noahtheduke.splint.rules.style.minus-one
   noahtheduke.splint.rules.style.minus-zero
   noahtheduke.splint.rules.style.multiple-arity-order
   noahtheduke.splint.rules.style.multiply-by-one
   noahtheduke.splint.rules.style.multiply-by-zero
   noahtheduke.splint.rules.style.neg-checks
   noahtheduke.splint.rules.style.nested-addition
   noahtheduke.splint.rules.style.nested-multiply
   noahtheduke.splint.rules.style.new-object
   noahtheduke.splint.rules.style.next-first
   noahtheduke.splint.rules.style.next-next
   noahtheduke.splint.rules.style.not-eq
   noahtheduke.splint.rules.style.not-nil
   noahtheduke.splint.rules.style.not-some-pred
   noahtheduke.splint.rules.style.plus-one
   noahtheduke.splint.rules.style.plus-zero
   noahtheduke.splint.rules.style.pos-checks
   noahtheduke.splint.rules.style.prefer-boolean
   noahtheduke.splint.rules.style.prefer-clj-math
   noahtheduke.splint.rules.style.prefer-clj-string
   noahtheduke.splint.rules.style.prefer-condp
   noahtheduke.splint.rules.style.prefer-for-with-literals
   noahtheduke.splint.rules.style.prefer-vary-meta
   noahtheduke.splint.rules.style.reduce-str
   noahtheduke.splint.rules.style.redundant-let
   noahtheduke.splint.rules.style.redundant-nested-call
   noahtheduke.splint.rules.style.redundant-regex-constructor
   noahtheduke.splint.rules.style.set-literal-as-fn
   noahtheduke.splint.rules.style.single-key-in
   noahtheduke.splint.rules.style.tostring
   noahtheduke.splint.rules.style.trivial-for
   noahtheduke.splint.rules.style.update-in-assoc
   noahtheduke.splint.rules.style.useless-do
   noahtheduke.splint.rules.style.when-do
   noahtheduke.splint.rules.style.when-not-call
   noahtheduke.splint.rules.style.when-not-do
   noahtheduke.splint.rules.style.when-not-empty
   noahtheduke.splint.rules.style.when-not-not))

(set! *warn-on-reflection* true)

(defn -main
  "Pass-through to runner which does all the work."
  [& args]
  (let [{:keys [exit]} (runner/run args)]
    (System/exit (or exit 0))))
