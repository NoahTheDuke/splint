; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace-test
  (:require
   [lazytest.core :refer [defdescribe it expect]]
   [noahtheduke.splint.replace :as sut]))

(set! *warn-on-reflection* true)

(defdescribe postwalk-splicing-replace-test
  (it "deref"
    (expect '(splint/deref b)
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/deref ?a)))
    (expect '(splint/deref (b))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/deref (?a)))))
  (it "fn"
    (expect '(splint/fn [arg] (+ b arg))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/fn [arg] (+ ?a arg)))))
  (it "read-eval"
    (expect '(splint/read-eval (+ b 2))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/read-eval (+ ?a 2)))))
  (it "re-pattern"
    (expect '(splint/re-pattern "asdfd")
      (sut/postwalk-splicing-replace {'?a "asdfd"} '(splint/re-pattern ?a))))
  (it "var"
    (expect '(splint/var b)
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/var ?a)))
    (expect '(splint/var (b c))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/var (?a c)))))
  (it "syntax-quote"
    (expect '(splint/syntax-quote b)
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/syntax-quote ?a)))
    (expect '(splint/syntax-quote (+ b ~b c#))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/syntax-quote (+ ?a ~b c#)))))
  (it "unquote"
    (expect '(splint/unquote b)
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote ?a)))
    (expect '(splint/unquote (b))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote (?a)))))
  (it "unquote-splicing"
    (expect '(splint/unquote-splicing b)
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote-splicing ?a)))
    (expect '(splint/unquote-splicing (b))
      (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote-splicing (?a))))))
