; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.replace :as sut]))

(defexpect postwalk-splicing-replace-test
  (expect '(clojure.core/deref b)
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/deref ?a)))
  (expect '(clojure.core/deref (b))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/deref (?a))))
  (expect '(clojure.core/fn [arg] (+ b arg))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/fn [arg] (+ ?a arg))))
  (expect (list (symbol "#=") '(+ 1 2))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/read-eval (+ 1 2))))
  (expect '(clojure.core/re-pattern "asdfd")
    (sut/postwalk-splicing-replace {'?a "asdfd"} '(splint/re-pattern ?a)))
  (expect '(var b)
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/var ?a)))
  (expect '(var (b c))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/var (?a c))))
  (expect (symbol "`b")
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/syntax-quote ?a)))
  (expect (list (symbol "`") '(+ b (clojure.core/unquote b) c#))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/syntax-quote (+ ?a ~b c#))))
  (expect '(clojure.core/unquote b)
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote ?a)))
  (expect '(clojure.core/unquote (b))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote (?a))))
  (expect '(clojure.core/unquote-splicing b)
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote-splicing ?a)))
  (expect '(clojure.core/unquote-splicing (b))
    (sut/postwalk-splicing-replace {'?a 'b} '(splint/unquote-splicing (?a)))))
