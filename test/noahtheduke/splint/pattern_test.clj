; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pattern-test
  (:require
   [lazytest.core :refer [defdescribe expect it throws? causes? describe]]
   [noahtheduke.splint.pattern :as sut]
   [noahtheduke.splint.test-helpers :refer [parse-string]]))

(set! *warn-on-reflection* true)

(defmacro bad-compile?
  [pattern input]
  `(fn [] (eval '(fn [] ((sut/pattern ~pattern) ~input)))))

(defdescribe read-dispatch-test
  (it "simple types"
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
      (expect (= t (sut/read-dispatch input)))))
  (it "refinements"
    (expect (= :any (sut/read-dispatch '_)))
    (expect (= :symbol (sut/read-dispatch (vary-meta '_ assoc :splint/lit true))))
    (doseq [[input t] [['(quote (1 2 3)) :quote]
                       ['(1 2 3) :list]]]
      (expect (= t (sut/read-dispatch input)))
      (expect (= :list (sut/read-dispatch (vary-meta input assoc :splint/lit true)))))))

(defdescribe pattern-test
  (describe '_
    (it "match any"
      (doseq [input '[a 'a :a "a" 1 true nil
                      (1 2 3)
                      {1 2}
                      #{1 2 3}
                      [2 2 3]]]
        (expect (= {} ((sut/pattern '(1 2 _)) (list 1 2 input)))))))

  (describe '?
    (it "can be a symbol"
      (expect (= '{?foo 1}
                ((sut/pattern '?foo) 1))))
    (it "can be a special pattern with ?-prefixed symbol"
      (expect (= '{?foo 1}
                ((sut/pattern '(? ?foo)) 1))))
    (it "can be a special pattern with a simple symbol"
      (expect (= '{?foo 1}
                ((sut/pattern '(? foo)) 1))))
    (it "uses a given predicate"
      (expect (= {} ((sut/pattern '(1 2 (? _ symbol?))) '(1 2 a))))
      (doseq [input '[:a "a" 1 true nil
                      (1 2 3)
                      {1 2}
                      #{1 2 3}
                      [1 2 3]]]
        (expect (nil? ((sut/pattern '(1 2 (? _ symbol?))) (list 1 2 input)))))
      (expect (= '{?a a}
                ((sut/pattern '(1 2 (? a symbol?)))
                  (parse-string "(1 2 a)")))))
    (it "rejects bad predicates"
      (expect (causes? RuntimeException
                (bad-compile? '(1 2 (? _ foo))
                  (parse-string "(1 2 3)"))))
      (expect (causes? RuntimeException
                (bad-compile? '(1 2 (? _ nil))
                  (parse-string "(1 2 3)"))))))

  (describe '?*
    (it "behaves correctly"
      (expect
        (= '{?exprs []}
          ((sut/pattern '[(?* exprs)])
            (parse-string "[]"))))
      (expect
        (= '{?exprs []}
          ((sut/pattern '(1 2 (?* exprs) 3 4))
            (parse-string "(1 2 3 4)"))))
      (expect
        (= '{?exprs [1 2 3]}
          ((sut/pattern '((?* exprs)))
            (parse-string "(1 2 3)"))))
      (expect
        (= '{?exprs [1 2 3]}
          ((sut/pattern '((?* exprs) (?* exprs)))
            (parse-string "(1 2 3 1 2 3)"))))
      (expect
        (= '{?exprs [1 2 3]}
          ((sut/pattern '((?* exprs) 1 (?* exprs)))
            (parse-string "(1 2 3 1 1 2 3)"))))
      (expect
        (nil?
          ((sut/pattern '((?* exprs) 1 (?* exprs)))
            (parse-string "(1 2 3 1 2 3)")))))
    (it "can be a simple symbol"
      (expect (= '{?foo [1]}
                ((sut/pattern '[?*foo])
                  (parse-string "[1]")))))
    (it "can use a predicate"
      (expect (= '{?foo [1]}
                ((sut/pattern '[(?* foo number?)])
                  (parse-string "[1]"))))
      (expect (nil?
                ((sut/pattern '[(?* foo symbol?)])
                  (parse-string "[1]")))))
    (it "can't be used alone"
      (expect (causes? IllegalArgumentException
                (bad-compile? '?*foo (parse-string "1"))))))

  (describe '?*
    (it "behaves correctly"
      (expect
        (nil?
          ((sut/pattern '[(?+ exprs)])
            (parse-string "[]"))))
      (expect
        (nil?
          ((sut/pattern '[1 2 (?+ exprs) 3 4])
            (parse-string "[1 2 3 4]"))))
      (expect
        (= '{?exprs [100]}
          ((sut/pattern '[1 2 (?+ exprs) 3 4])
            (parse-string "[1 2 100 3 4]"))))
      (expect
        (= '{?exprs [1 2 3]}
          ((sut/pattern '((?+ exprs)))
            (parse-string "(1 2 3)"))))
      (expect
        (= '{?exprs [1 2 3]}
          ((sut/pattern '((?+ exprs) (?+ exprs)))
            (parse-string "(1 2 3 1 2 3)"))))
      (expect
        (nil?
          ((sut/pattern '((?+ exprs) 1 (?+ exprs)))
            (parse-string "(1 2 3 1 2 3)")))))
    (it "can be a simple symbol"
      (expect (= '{?foo [1]}
                ((sut/pattern '[?+foo])
                  (parse-string "[1]")))))
    (it "can use a predicate"
      (expect (= '{?foo [1]}
                ((sut/pattern '[(?+ foo number?)])
                  (parse-string "[1]"))))
      (expect (nil?
                ((sut/pattern '[(?+ foo symbol?)])
                  (parse-string "[1]")))))
    (it "can't be used alone"
      (expect (causes? IllegalArgumentException
                (bad-compile? '?+foo (parse-string "1"))))))

  (describe '??
    (it "behaves correctly"
      (expect
        (= '{?exprs []}
          ((sut/pattern '[(?? exprs)])
            (parse-string "[]"))))
      (expect
        (= '{?exprs []}
          ((sut/pattern '(if x y (?? exprs nil?)))
            (parse-string "(if x y)"))))
      (expect
        (= '{?exprs [nil]}
          ((sut/pattern '(if x y (?? exprs nil?)))
            (parse-string "(if x y nil)"))))
      (expect
        (= '{?exprs []}
          ((sut/pattern '[1 2 (?? exprs) 3 4])
            (parse-string "[1 2 3 4]"))))
      (expect
        (= '{?exprs [100]}
          ((sut/pattern '[1 2 (?? exprs) 3 4])
            (parse-string "[1 2 100 3 4]"))))
      (expect
        (= '{?exprs [1]}
          ((sut/pattern '[(?? exprs) (?? exprs)])
            (parse-string "[1 1]"))))
      (expect
        (= '{?a []
             ?b [1]}
          ((sut/pattern '[(?? a) (?? b)])
            (parse-string "[1]"))))
      (expect
        (nil?
          ((sut/pattern '[(?? exprs) (?? exprs)])
            (parse-string "[1 2]"))))
      (expect
        (nil?
          ((sut/pattern '((?? exprs)))
            (parse-string "(1 2 3)")))))
    (it "can be a simple symbol"
      (expect (= '{?foo [1]}
                ((sut/pattern '[??foo])
                  (parse-string "[1]")))))
    (it "can use a predicate"
      (expect (= '{?foo [1]}
                ((sut/pattern '[(?? foo number?)])
                  (parse-string "[1]"))))
      (expect (nil?
                ((sut/pattern '[(?? foo symbol?)])
                  (parse-string "[1]")))))
    (it "can't be used alone"
      (expect (causes? IllegalArgumentException
                (bad-compile? '??foo (parse-string "1"))))))

  (describe '?|
    (it "behaves correctly"
      (expect
        (nil?
          ((sut/pattern '[(?| exprs [1 2])])
            (parse-string "[]"))))
      (expect
        (= '{?exprs 1}
          ((sut/pattern '[(?| exprs [1 2])])
            (parse-string "[1]"))))
      (expect
        (= '{?exprs 2}
          ((sut/pattern '[0 (?| exprs [1 2]) 3 4])
            (parse-string "[0 2 3 4]"))))
      (expect
        (nil?
          ((sut/pattern '[0 (?| exprs [1 2]) 3 4 (?| exprs [1 2])])
            (parse-string "[0 2 3 4 1]"))))
      (expect
        (= '{?exprs 2}
          ((sut/pattern '[0 (?| exprs [1 2]) 3 4 (?| exprs [1 2])])
            (parse-string "[0 2 3 4 2]"))))
      (expect
        (= '{?exprs 1}
          ((sut/pattern '[(?| exprs [1 2]) 3])
            (parse-string "[1 3]"))))
      (expect
        (= '{?exprs 2}
          ((sut/pattern '[(?| exprs [1 2]) 3])
            (parse-string "[2 3]")))))
    (it "doesn't have a short-form"
      (expect
        (causes? IllegalArgumentException
          (bad-compile? '(?|exprs)
            (parse-string "(1 2 3)"))))
      (expect
        (causes? IllegalArgumentException
          (bad-compile? '((?| exprs))
            (parse-string "(1 2 3)")))))
    (it "rejects empty vectors"
      (expect
        (causes? IllegalArgumentException
          (bad-compile? '((?| exprs []))
            (parse-string "(1 2 3)")))))
    (it "can't be used alone"
      (expect
        (causes? IllegalArgumentException
          (bad-compile? '?|exprs
            (parse-string "(1 2 3)"))))))

  (it "quote"
    (expect
      (= '{}
         ((sut/pattern '(a b 'c))
          (parse-string "(a b 'c)")))))

  (it "literals"
    ;; [[sut/pattern]] calls drop-quote so have to double up on
    ;; quoted form
    (expect ((sut/pattern 'a) (parse-string "a")))
    (expect ((sut/pattern ''a) (parse-string "'a")))
    (expect ((sut/pattern ':a) (parse-string ":a")))
    (expect ((sut/pattern '"a") (parse-string "\"a\"")))
    (expect ((sut/pattern '1) (parse-string "1")))
    (expect ((sut/pattern 'true) (parse-string "true")))
    (expect ((sut/pattern 'nil) (parse-string "nil"))))

  (it "lists"
    (let [pat (sut/pattern '(:a 1 :b [2] :c {:d 3}))]
      (expect (not (pat (parse-string "(:a 1 :b [2] :c {:e 4})"))))
      (expect (pat (parse-string "(:a 1 :b [2] :c {:d 3})")))
      (expect (not (pat (parse-string "(:a 1 :b [2] :c {:d 3} :e 4)"))))))

  (it "vectors"
    (let [pat (sut/pattern '[:a 1 :b [2] :c {:d 3}])]
      (expect (not (pat (parse-string "[:a 1 :b [2] :c {:e 4}]"))))
      (expect (pat (parse-string "[:a 1 :b [2] :c {:d 3}]")))
      (expect (not (pat (parse-string "[:a 1 :b [2] :c {:d 3} :e 4]")))))))

#_(defdescribe map-test
    (let [pat (sut/pattern '{:a 1 :b [2] :c {:d 3}})]
      (expect not (pat (parse-string "{:a 1 :b [2] :c {:e 4}}")))
      (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3}}")))
      (expect (pat (parse-string "{:a 1 :b [2] :c {:d 3} :e 4}")))))

#_(defdescribe set-test
    (let [pat (sut/pattern '#{:a 1 :b [2] :c {:d 3}})]
      (expect not (pat (parse-string "#{:a 1 :b [2] :c}")))
      (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3}}")))
      (expect (pat (parse-string "#{:a 1 :b [2] :c {:d 3} :e [4]}")))))

(defdescribe expand-specials-test
  (it "expands correctly"
    (expect (= 'a (sut/expand-specials 'a)))
    (expect (= '(a b c) (sut/expand-specials '(a b c))))
    (expect (= '(a _ c) (sut/expand-specials '(a _ c))))
    (expect (= '(a (b) c) (sut/expand-specials '(a (b) c))))
    (expect (= '(a ?b c) (sut/expand-specials '(a ?b c))))
    (expect (= '(a (?b) c) (sut/expand-specials '(a (?b) c))))
    (expect (= '(a (?+ b) c) (sut/expand-specials '(a ?+b c))))
    (expect (= '(a (?+ ?b) c) (sut/expand-specials '(a (?+ ?b) c))))
    (expect (= '(a (?* b) c) (sut/expand-specials '(a ?*b c))))
    (expect (= '(a (?* ?b) c) (sut/expand-specials '(a (?* ?b) c))))
    (expect (= '(a (?? b) c) (sut/expand-specials '(a ??b c))))
    (expect (= '(a (?? ?b) c) (sut/expand-specials '(a (?? ?b) c))))
    (expect (= '(a (?| ?b) c) (sut/expand-specials '(a (?| ?b) c))))
    (expect (throws? IllegalArgumentException #(sut/expand-specials '(a ?|b c))))))
