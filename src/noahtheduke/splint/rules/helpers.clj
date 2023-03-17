; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.helpers)

(defn deref?? [sexp]
  (case sexp
    (deref splint/deref) true
    false))

(defn syntax-quote?? [sexp]
  (= 'splint/syntax-quote sexp))

(defn unquote?? [sexp]
  (case sexp
    (unquote splint/unquote) true
    false))

(defn unquote-splicing?? [sexp]
  (case sexp
    (unquote-splicing splint/unquote-splicing) true
    false))

(defn var?? [sexp]
  (case sexp
    (var splint/var) true
    false))

(defn read-eval?? [sexp]
  (= 'splint/read-eval sexp))

(defn fn?? [sexp]
  (case sexp
    (fn fn* splint/fn) true
    false))

(defn re-pattern?? [sexp]
  (case sexp
    (re-pattern splint/re-pattern) true
    false))
