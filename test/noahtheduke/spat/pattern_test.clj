; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.pattern-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each]]
    [noahtheduke.splint.parser :refer [parse-string]]
    [noahtheduke.spat.pattern :refer [pattern]]))

(defexpect match-any-test
  (expect {}
    (from-each [input '[a 'a :a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((pattern '(1 2 _)) (list 1 2 input)))))

(defexpect predicate-test
  (expect {}
    ((pattern '(1 2 %symbol?)) '(1 2 a)))
  (expect nil?
    (from-each [input '['a :a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((pattern '(1 2 %symbol?)) (list 1 2 input))))
  (expect '{?a a}
    ((pattern '(1 2 %symbol?%-?a)) '(1 2 a))))

(defexpect multiple-rest-body-test
  '{?test (= 1 1)
    ?exprs ((prn 1) (prn 2))
    ?foo (foo bar)}
  ((pattern '(when ?test &&. ?exprs ?foo (recur)))
   (parse-string "(when (= 1 1) (prn 1) (prn 2) (foo bar) (recur))")))

(defexpect quote-in-pattern-test
  '{}
  ((pattern '(a b 'c))
   (parse-string "(a b 'c)")))

(defexpect literals-test
  ;; [[pattern]] calls drop-quote so have to double up on
  ;; quoted form
  (expect (pattern 'a) (parse-string "a"))
  (expect (pattern ''a) (parse-string "'a"))
  (expect (pattern ':a) (parse-string ":a"))
  (expect (pattern '"a") (parse-string "\"a\""))
  (expect (pattern '1) (parse-string "1"))
  (expect (pattern 'true) (parse-string "true"))
  (expect (pattern 'nil) (parse-string "nil")))

(defexpect list-test
  (let [pat (pattern '(:a 1 :b [2] :c {:d 3}))]
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:e 4})")))
    (expect (pat (parse-string "(:a 1 :b [2] :c {:d 3})")))
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:d 3} :e 4)")))))

(defexpect map-test
  (let [pat (pattern '{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "{:a 1 :b [2] :c {:e 4}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3} :e 4}")))))

(defexpect set-test
  (let [pat (pattern '#{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "#{:a 1 :b [2] :c}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3} :e [4]}")))))

(defexpect vector-test
  (let [pat (pattern '[:a 1 :b [2] :c {:d 3}])]
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:e 4}]")))
    (expect (pat (parse-string "[:a 1 :b [2] :c {:d 3}]")))
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:d 3} :e 4]")))))
