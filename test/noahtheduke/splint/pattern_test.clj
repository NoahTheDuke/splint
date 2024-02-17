; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pattern-test
  (:require
    [clojure.test :refer [deftest]] 
    [expectations.clojure.test :refer [expect from-each expecting]]
    [noahtheduke.splint.pattern :as sut]
    [noahtheduke.splint.test-helpers :refer [parse-string]]))

(set! *warn-on-reflection* true)

(deftest read-dispatch-test
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
      (expect (sut/read-dispatch input) t)))
  (expecting "refinements"
    (expect :any (sut/read-dispatch '_))
    (expect :symbol (sut/read-dispatch (vary-meta '_ assoc :splint/lit true)))
    (doseq [[input t] [['(quote (1 2 3)) :quote]
                       ['(1 2 3) :list]]]
      (expect t (sut/read-dispatch input))
      (expect :list (sut/read-dispatch (vary-meta input assoc :splint/lit true))))))

(deftest match-any-test
  (expect {}
    (from-each [input '[a 'a :a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((sut/pattern '(1 2 _)) (list 1 2 input)))))

(deftest predicate-test
  (expect {}
    ((sut/pattern '(1 2 (? _ symbol?))) '(1 2 a)))
  (expect nil?
    (from-each [input '[:a "a" 1 true nil
                        (1 2 3)
                        {1 2}
                        #{1 2 3}
                        [1 2 3]]]
      ((sut/pattern '(1 2 (? _ symbol?))) (list 1 2 input))))
  (expect '{?a a}
    ((sut/pattern '(1 2 (? a symbol?))) '(1 2 a))))

(deftest ?*-test
  (expect
    '{?exprs []}
    ((sut/pattern '[(?* exprs)])
     (parse-string "[]")))
  (expect
    '{?exprs []}
    ((sut/pattern '[1 2 (?* exprs) 3 4])
     (parse-string "[1 2 3 4]")))
  (expect
    '{?exprs [1 2 3]}
    ((sut/pattern '((?* exprs)))
     (parse-string "(1 2 3)")))
  (expect
    '{?exprs [1 2 3]}
    ((sut/pattern '((?* exprs) (?* exprs)))
     (parse-string "(1 2 3 1 2 3)")))
  (expect
    '{?exprs [1 2 3]}
    ((sut/pattern '((?* exprs) 1 (?* exprs)))
     (parse-string "(1 2 3 1 1 2 3)")))
  (expect
    nil?
    ((sut/pattern '((?* exprs) 1 (?* exprs)))
     (parse-string "(1 2 3 1 2 3)"))))

(deftest ?+-test
  (expect
    nil?
    ((sut/pattern '[(?+ exprs)])
     (parse-string "[]")))
  (expect
    nil?
    ((sut/pattern '[1 2 (?+ exprs) 3 4])
     (parse-string "[1 2 3 4]")))
  (expect
    '{?exprs [100]}
    ((sut/pattern '[1 2 (?+ exprs) 3 4])
     (parse-string "[1 2 100 3 4]")))
  (expect
    '{?exprs [1 2 3]}
    ((sut/pattern '((?+ exprs)))
     (parse-string "(1 2 3)")))
  (expect
    '{?exprs [1 2 3]}
    ((sut/pattern '((?+ exprs) (?+ exprs)))
     (parse-string "(1 2 3 1 2 3)")))
  (expect
    nil?
    ((sut/pattern '((?+ exprs) 1 (?+ exprs)))
     (parse-string "(1 2 3 1 2 3)"))))

(deftest ??-test
  (expect
    '{?exprs []}
    ((sut/pattern '[(?? exprs)])
     (parse-string "[]")))
  (expect
    '{?exprs []}
    ((sut/pattern '[1 2 (?? exprs) 3 4])
     (parse-string "[1 2 3 4]")))
  (expect
    '{?exprs [100]}
    ((sut/pattern '[1 2 (?? exprs) 3 4])
     (parse-string "[1 2 100 3 4]")))
  (expect
    '{?exprs [1]}
    ((sut/pattern '[(?? exprs) (?? exprs)])
     (parse-string "[1 1]")))
  (expect
    '{?a []
      ?b [1]}
    ((sut/pattern '[(?? a) (?? b)])
     (parse-string "[1]")))
  (expect
    nil?
    ((sut/pattern '[(?? exprs) (?? exprs)])
     (parse-string "[1 2]")))
  (expect
    nil?
    ((sut/pattern '((?? exprs)))
     (parse-string "(1 2 3)"))))

(deftest ?|-test
  (expect
    nil?
    ((sut/pattern '[(?| exprs [1 2])])
     (parse-string "[]")))
  (expect
    '{?exprs 1}
    ((sut/pattern '[(?| exprs [1 2])])
     (parse-string "[1]")))
  (expect
    '{?exprs 2}
    ((sut/pattern '[0 (?| exprs [1 2]) 3 4])
     (parse-string "[0 2 3 4]")))
  (expect
    nil?
    ((sut/pattern '[0 (?| exprs [1 2]) 3 4 (?| exprs [1 2])])
     (parse-string "[0 2 3 4 1]")))
  (expect
    '{?exprs 2}
    ((sut/pattern '[0 (?| exprs [1 2]) 3 4 (?| exprs [1 2])])
     (parse-string "[0 2 3 4 2]")))
  (expect
    nil?
    ((sut/pattern '((?| exprs [])))
     (parse-string "(1 2 3)")))
  (expect
    '{?exprs 1}
    ((sut/pattern '[(?| exprs [1 2]) 3])
     (parse-string "[1 3]")))
  (expect
    '{?exprs 2}
    ((sut/pattern '[(?| exprs [1 2]) 3])
     (parse-string "[2 3]"))))

(deftest quote-in-pattern-test
  '{}
  ((sut/pattern '(a b 'c))
   (parse-string "(a b 'c)")))

(deftest literals-test
  ;; [[sut/pattern]] calls drop-quote so have to double up on
  ;; quoted form
  (expect (sut/pattern 'a) (parse-string "a"))
  (expect (sut/pattern ''a) (parse-string "'a"))
  (expect (sut/pattern ':a) (parse-string ":a"))
  (expect (sut/pattern '"a") (parse-string "\"a\""))
  (expect (sut/pattern '1) (parse-string "1"))
  (expect (sut/pattern 'true) (parse-string "true"))
  (expect (sut/pattern 'nil) (parse-string "nil")))

(deftest list-test
  (let [pat (sut/pattern '(:a 1 :b [2] :c {:d 3}))]
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:e 4})")))
    (expect (pat (parse-string "(:a 1 :b [2] :c {:d 3})")))
    (expect not (pat (parse-string "(:a 1 :b [2] :c {:d 3} :e 4)")))))

#_(deftest map-test
  (let [pat (sut/pattern '{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "{:a 1 :b [2] :c {:e 4}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3} :e 4}")))))

#_(deftest set-test
  (let [pat (sut/pattern '#{:a 1 :b [2] :c {:d 3}})]
    (expect not (pat (parse-string "#{:a 1 :b [2] :c}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3}}")))
    (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3} :e [4]}")))))

(deftest vector-test
  (let [pat (sut/pattern '[:a 1 :b [2] :c {:d 3}])]
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:e 4}]")))
    (expect (pat (parse-string "[:a 1 :b [2] :c {:d 3}]")))
    (expect not (pat (parse-string "[:a 1 :b [2] :c {:d 3} :e 4]")))))

(deftest expand-specials-test
  (expect 'a (sut/expand-specials 'a))
  (expect '(a b c) (sut/expand-specials '(a b c)))
  (expect '(a _ c) (sut/expand-specials '(a _ c)))
  (expect '(a (b) c) (sut/expand-specials '(a (b) c)))
  (expect '(a (? b) c) (sut/expand-specials '(a ?b c)))
  (expect '(a (?+ b) c) (sut/expand-specials '(a ?+b c)))
  (expect '(a (?* b) c) (sut/expand-specials '(a ?*b c)))
  (expect '(a (?? b) c) (sut/expand-specials '(a ??b c)))
  (expect IllegalArgumentException (sut/expand-specials '(a ?|b c))))
