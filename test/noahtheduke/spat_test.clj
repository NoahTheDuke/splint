; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [clojure.test :refer [deftest is]]
            [noahtheduke.spat]
            [noahtheduke.spat.pattern :refer [pattern]]
            [noahtheduke.spat.rules :refer [global-rules]]
            [noahtheduke.spat.runner :refer [parse-string check-form]]))

(set! *warn-on-reflection* true)

(defexpect multiple-rest-body
  '{?test (= 1 1)
    ?exprs ((prn 1) (prn 2))
    ?foo (foo bar)}
  ((pattern '(when ?test &&. ?exprs ?foo (recur)))
   (parse-string "(when (= 1 1) (prn 1) (prn 2) (foo bar) (recur))")))

(defexpect quote-in-pattern
  '{}
  ((pattern '(a b 'c))
   (parse-string "(a b 'c)")))

(defn check-str
  [s]
  (let [ctx (atom {})
        form (parse-string s)]
    (check-form ctx @global-rules form)))

(defn replacement
  [s]
  (:alt (check-str s)))

(defexpect str-to-string-test
  '(str x)
  (replacement "(.toString x)"))

(defexpect dot-obj-usage-test
  '(.method obj 1 2 3)
  (replacement "(. obj method 1 2 3)"))

(defexpect dot-class-usage-test
  '(Obj/method 1 2 3)
  (replacement "(. Obj method 1 2 3)"))

(defexpect str-apply-interpose-test
  '(clojure.string/join x y)
  (replacement "(apply str (interpose x y))"))

(defexpect str-apply-reverse-test
  '(clojure.string/reverse x)
  (replacement "(apply str (reverse x))"))

(defexpect str-apply-str-test
  '(clojure.string/join x)
  (replacement "(apply str x)"))

(defexpect mapcat-apply-apply-test
  '(mapcat x y)
  (replacement "(apply concat (apply map x y))"))

(defexpect mapcat-concat-map-test
  '(mapcat x y z)
  (replacement "(apply concat (map x y z))"))

(defexpect filter-complement-test
  '(remove pred coll)
  (replacement "(filter (complement pred) coll)"))

(defexpect filter-seq-test
  '(remove empty? coll)
  (replacement "(filter seq coll)"))

(defexpect filter-fn*-not-pred-test
  '(remove pred coll)
  (replacement "(filter (fn* [x] (not (pred x))) coll)"))

(defexpect filter-fn-not-pred-test
  '(remove pred coll)
  (replacement "(filter (fn [x] (not (pred x))) coll)"))

(defexpect filter-vec-filter-test
  '(filterv pred coll)
  (replacement "(vec (filter pred coll))"))

(defexpect first-first-test
  '(ffirst coll)
  (replacement "(first (first coll))"))

(defexpect first-next-test
  '(fnext coll)
  (replacement "(first (next coll))"))

(defexpect next-first-test
  '(nfirst coll)
  (replacement "(next (first coll))"))

(defexpect next-next-test
  '(nnext coll)
  (replacement "(next (next coll))"))

(defexpect fn*-wrapper-test
  'f
  (replacement "(fn* [arg] (f arg))"))

(defexpect fn-wrapper-test
  'f
  (replacement "(fn [arg] (f arg))"))

(defexpect thread-first-no-arg-test
  'x
  (replacement "(-> x)"))

(defexpect thread-first-1-arg-test
  (expect '(f arg) (replacement "(-> arg f)"))
  (expect '(f arg) (replacement "(-> arg (f))")))

(defexpect thread-last-no-arg-test
  'x
  (replacement "(->> x)"))

(defexpect thread-last-1-arg-test
  (expect '(form arg) (replacement "(->> arg form)"))
  (expect '(form arg) (replacement "(->> arg (form))")))

(defexpect not-some-pred-test
  '(not-any? pred coll)
  (replacement "(not (some pred coll))"))

(defexpect with-meta-f-meta-test
  '(vary-meta x f args)
  (replacement "(with-meta x (f (meta x) args))"))

(defexpect plus-x-1-test
  '(inc x)
  (replacement "(+ x 1)"))

(defexpect plus-1-x-test
  '(inc x)
  (replacement "(+ 1 x)"))

(defexpect minus-x-1-test
  '(dec x)
  (replacement "(- x 1)"))

(defexpect nested-muliply-test
  '(* x xs)
  (replacement "(* x (* xs))"))

(defexpect nested-addition-test
  '(+ x xs)
  (replacement "(+ x (+ xs))"))

(defexpect plus-0-test
  'x
  (replacement "(+ x 0)"))

(defexpect minus-0-test
  'x
  (replacement "(- x 0)"))

(defexpect multiply-by-1-test
  'x
  (replacement "(* x 1)"))

(defexpect divide-by-1-test
  'x
  (replacement "(/ x 1)"))

(defexpect multiply-by-0-test
  '0
  (replacement "(* x 0)"))

(defexpect conj-vec-test
  '(vector x)
  (replacement "(conj [] x)"))

(defexpect into-vec-test
  '(vec coll)
  (replacement "(into [] coll)"))

(defexpect assoc-assoc-key-coll-test
  '(assoc-in coll [:k1 :k2] v)
  (replacement "(assoc coll :k1 (assoc (:k1 coll) :k2 v))"))

(defexpect assoc-assoc-coll-key-test
  '(assoc-in coll [:k1 :k2] v)
  (replacement "(assoc coll :k1 (assoc (coll :k1) :k2 v))"))

(defexpect assoc-assoc-get-test
  '(assoc-in coll [:k1 :k2] v)
  (replacement "(assoc coll :k1 (assoc (get coll :k1) :k2 v))"))

(defexpect assoc-fn-key-coll-test
  '(update coll :k f args)
  (replacement "(assoc coll :k (f (:k coll) args))"))

(defexpect assoc-fn-coll-key-test
  '(update coll :k f args)
  (replacement "(assoc coll :k (f (coll :k) args))"))

(defexpect assoc-fn-get-test
  '(update coll :k f args)
  (replacement "(assoc coll :k (f (get coll :k) args))"))

(defexpect update-in-assoc-test
  '(assoc-in coll ks v)
  (replacement "(update-in coll ks assoc v)"))

(defexpect not-empty?-test
  '(seq x)
  (replacement "(not (empty? x))"))

(defexpect when-not-empty?-test
  (expect '(when (seq x) y) (replacement "(when-not (empty? x) y)"))
  (expect nil? (replacement "(if (= 1 called-with) \"arg\" \"args\")")))

(defexpect into-set-test
  '(set coll)
  (replacement "(into #{} coll)"))

(defexpect take-repeatedly-test
  '(repeatedly n coll)
  (replacement "(take n (repeatedly coll))"))

(defexpect dorun-map-test
  '(run! f coll)
  (replacement "(dorun (map f coll))"))

(defexpect if-else-nil-test
  (expect '(when x y) (replacement "(if x y nil)"))
  (expect nil? (replacement "(if x \"y\" \"z\")")))

(defexpect if-nil-else-test
  '(when-not x y)
  (replacement "(if x nil y)"))

(defexpect if-then-do-test
  '(when x y)
  (replacement "(if x (do y))"))

(defexpect if-not-x-y-x-test
  '(if-not x y z)
  (replacement "(if (not x) y z)"))

(defexpect if-x-x-y-test
  '(or x y)
  (replacement "(if x x y)"))

(defexpect when-not-x-y-test
  '(when-not x y)
  (replacement "(when (not x) y)"))

(defexpect useless-do-x-test
  'x
  (replacement "(do x)"))

(defexpect if-let-else-nil-test
  '(when-let ?binding ?expr)
  (replacement "(if-let ?binding ?expr nil)"))

(defexpect when-do-test
  '(when x y)
  (replacement "(when x (do y))"))

(defexpect let-when-test
  '(when-let [result (some-func)] (do-stuff result))
  (replacement "(let [result (some-func)] (when result (do-stuff result)))"))

(defexpect let-if-test
  '(if-let [result (some-func)] (do-stuff result) (other-stuff))
  (replacement "(let [result (some-func)] (if result (do-stuff result) (other-stuff)))"))

(defexpect when-not-do-test
  '(when-not x y)
  (replacement "(when-not x (do y))"))

(defexpect if-not-do-test
  '(when-not x y)
  (replacement "(if-not x (do y))"))

(defexpect if-not-not-test
  '(if x y z)
  (replacement "(if-not (not x) y z)"))

(defexpect when-not-not-test
  '(when x y)
  (replacement "(when-not (not x) y)"))

(defexpect loop-empty-when-test
  '(while (= 1 1) (prn 1) (prn 2))
  (replacement "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"))

(defexpect let-do-test
  '(let [a 1 b 2] (prn a b))
  (replacement "(let [a 1 b 2] (do (prn a b)))"))

(defexpect loop-do-test
  '(loop [] 1)
  (replacement "(loop [] (do 1))"))

(defexpect cond-else-test
  (expect '(cond (pos? x) (inc x) :else -1)
    (replacement "(cond (pos? x) (inc x) :default -1)"))
  (expect '(cond (pos? x) (inc x) :else -1)
    (replacement "(cond (pos? x) (inc x) true -1)"))
  (expect nil? (replacement "(cond (pos? x) (inc x) (neg? x) (dec x))"))
  (expect nil? (replacement "(cond :else true)")))

(defexpect not-eq-test
  '(not= arg1 arg2 arg3)
  (replacement "(not (= arg1 arg2 arg3))"))

(defexpect eq-0-x-test
  '(zero? x)
  (replacement "(= 0 x)"))

(defexpect eq-x-0-test
  '(zero? x)
  (replacement "(= x 0)"))

(defexpect eqeq-0-x-test
  '(zero? x)
  (replacement "(== 0 x)"))

(defexpect eqeq-x-0-test
  '(zero? x)
  (replacement "(== x 0)"))

(defexpect lt-0-x-test
  '(pos? x)
  (replacement "(< 0 x)"))

(defexpect lt-x-0-test
  '(neg? x)
  (replacement "(< x 0)"))

(defexpect gt-0-x-test
  '(neg? x)
  (replacement "(> 0 x)"))

(defexpect gt-x-0-test
  '(pos? x)
  (replacement "(> x 0)"))

(defexpect eq-true-test
  '(true? x)
  (replacement "(= true x)"))

(defexpect eq-false-test
  '(false? x)
  (replacement "(= false x)"))

(defexpect eq-x-nil-test
  '(nil? x)
  (replacement "(= x nil)"))

(defexpect eq-nil-x-test
  '(nil? x)
  (replacement "(= nil x)"))

(defexpect not-nil?-test
  '(some? x)
  (replacement "(not (nil? x))"))

(defexpect missing-body-in-when-test
  "Missing body in when"
  (:message (check-str "(when true)")))
