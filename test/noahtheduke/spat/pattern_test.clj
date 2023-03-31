; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.pattern-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each expecting]]
    [noahtheduke.spat.parser :refer [parse-string]]
    [noahtheduke.spat.pattern :as sut]))

(defexpect read-dispatch-test
  (expecting "simple types"
    (doseq [[input t] '[[nil :nil]
                        [true :boolean]
                        [1 :number]
                        ["a" :string]
                        [:a :keyword]
                        [a :symbol]
                        [(1 2 3) :list]
                        [[1 2 3] :vector]
                        [{1 2} :map]
                        [#{1 2 3} :set]]]
      (expect (sut/read-dispatch input nil nil) t)))
  (expecting ":symbol refinements"
    (expect (sut/read-dispatch '_ nil nil) :any)
    (expect (sut/read-dispatch '^:spat/lit _ nil nil) :symbol)
    (expect (sut/read-dispatch '%asdf nil nil) :pred)
    (expect (sut/read-dispatch '^:spat/lit %asdf nil nil) :symbol)
    (expect (sut/read-dispatch '?asdf nil nil) :binding)
    (expect (sut/read-dispatch '^:spat/lit ?asdf nil nil) :symbol)
    (expect (sut/read-dispatch '&&. nil nil) :rest)
    (expect (sut/read-dispatch '^:spat/lit &&. nil nil) :symbol))
  (expecting ":list refinements")
  (expect (sut/read-dispatch '(quote 1) nil nil) :quote)
  (expect (sut/read-dispatch '^:spat/lit (quote 1) nil nil) :list))

(defexpect match-any-test
  (expect {}
    (from-each [input '[a 'a :a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((sut/pattern '(1 2 _)) (list 1 2 input)))))

(defexpect predicate-test
  (expect {}
    ((sut/pattern '(1 2 %symbol?)) '(1 2 a)))
  (expect nil?
    (from-each [input '['a :a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((sut/pattern '(1 2 %symbol?)) (list 1 2 input))))
  (expect '{?a a}
    ((sut/pattern '(1 2 %symbol?%-?a)) '(1 2 a))))

(defexpect multiple-rest-body-test
  '{?test (= 1 1)
    ?exprs ((prn 1) (prn 2))
    ?foo (foo bar)}
  ((sut/pattern '(when ?test &&. ?exprs ?foo (recur)))
   (parse-string "(when (= 1 1) (prn 1) (prn 2) (foo bar) (recur))")))

(defexpect quote-in-pattern-test
  '{}
  ((sut/pattern '(a b 'c))
   (parse-string "(a b 'c)")))

(defexpect literals-test
  ;; [[sut/pattern]] calls drop-quote so have to double up on
  ;; quoted form
  (expect (sut/pattern 'a) (parse-string "a"))
  (expect (sut/pattern ''a) (parse-string "'a"))
  (expect (sut/pattern ':a) (parse-string ":a"))
  (expect (sut/pattern '"a") (parse-string "\"a\""))
  (expect (sut/pattern '1) (parse-string "1"))
  (expect (sut/pattern 'true) (parse-string "true"))
  (expect (sut/pattern 'nil) (parse-string "nil")))

(defexpect list-test
  (let [pat (sut/pattern '(:a 1 :b [2] :c {:d 3}))]
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:e 4})")))
    (expect (pat (parse-string "(:a 1 :b [2] :c {:d 3})")))
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:d 3} :e 4)")))))

(defexpect map-test
  (let [pat (sut/pattern '{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "{:a 1 :b [2] :c {:e 4}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3} :e 4}")))))

(defexpect set-test
  (let [pat (sut/pattern '#{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "#{:a 1 :b [2] :c}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3} :e [4]}")))))

(defexpect vector-test
  (let [pat (sut/pattern '[:a 1 :b [2] :c {:d 3}])]
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:e 4}]")))
    (expect (pat (parse-string "[:a 1 :b [2] :c {:d 3}]")))
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:d 3} :e 4]")))))
