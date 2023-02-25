; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [noahtheduke.spat.pattern :refer [pattern]]
            [noahtheduke.spat.rules :refer [global-rules]]
            [noahtheduke.spat.runner :refer [parse-string check-subforms]]))

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
  (let [ctx (atom {})]
    (check-subforms ctx @global-rules "filename.clj" (parse-string s))
    (-> @ctx :violations first :alt)))

(defexpect str-to-string-test
  '(str x)
  (check-str "(.toString x)"))

(defexpect dot-obj-usage-test
  '(.method obj 1 2 3)
  (check-str "(. obj method 1 2 3)"))

(defexpect dot-class-usage-test
  '(Obj/method  1 2 3)
  (check-str "(. Obj method 1 2 3)"))

(defexpect str-apply-interpose-test
  '(clojure.string/join x y)
  (check-str "(apply str (interpose x y))"))

(defexpect str-apply-reverse-test
  '(clojure.string/reverse x)
  (check-str "(apply str (reverse x))"))

(defexpect str-apply-str-test
  '(clojure.string/join x)
  (check-str "(apply str x)"))

(defexpect mapcat-apply-apply-test
  '(mapcat x y)
  (check-str "(apply concat (apply map x y))"))

(defexpect mapcat-concat-map-test
  '(mapcat x y z)
  (check-str "(apply concat (map x y z))"))

(defexpect filter-complement-test
  '(remove pred coll)
  (check-str "(filter (complement pred) coll)"))

(defexpect filter-seq-test
  '(remove empty? coll)
  (check-str "(filter seq coll)"))

(defexpect filter-fn*-not-pred-test
  '(remove pred coll)
  (check-str "(filter (fn* [x] (not (pred x))) coll)"))

(defexpect filter-fn-not-pred-test
  '(remove pred coll)
  (check-str "(filter (fn [x] (not (pred x))) coll)"))

(defexpect filter-vec-filter-test
  '(filterv pred coll)
  (check-str "(vec (filter pred coll))"))

(defexpect first-first-test
  '(ffirst coll)
  (check-str "(first (first coll))"))

(defexpect first-next-test
  '(fnext coll)
  (check-str "(first (next coll))"))

(defexpect next-first-test
  '(nfirst coll)
  (check-str "(next (first coll))"))

(defexpect next-next-test
  '(nnext coll)
  (check-str "(next (next coll))"))

(defexpect fn*-wrapper-test
  'f
  (check-str "(fn* [arg] (f arg))"))

(defexpect fn-wrapper-test
  'f
  (check-str "(fn [arg] (f arg))"))

(defexpect thread-first-no-arg-test
  'x
  (check-str "(-> x)"))

(defexpect thread-first-1-arg-test
  (expect '(f arg) (check-str "(-> arg f)"))
  (expect '(f arg) (check-str "(-> arg (f))")))

(defexpect thread-last-no-arg-test
  'x
  (check-str "(->> x)"))

(defexpect thread-last-1-arg-test
  (expect '(form arg) (check-str "(->> arg form)"))
  (expect '(form arg) (check-str "(->> arg (form))")))

(defexpect not-some-pred-test
  '(not-any? pred coll)
  (check-str "(not (some pred coll))"))

(defexpect with-meta-f-meta-test
  '(vary-meta x f args)
  (check-str "(with-meta x (f (meta x) args))"))

(defexpect plus-x-1-test
  '(inc x)
  (check-str "(+ x 1)"))

(defexpect plus-1-x-test
  '(inc x)
  (check-str "(+ 1 x)"))

(defexpect minus-x-1-test
  '(dec x)
  (check-str "(- x 1)"))

(defexpect nested-muliply-test
  '(* x xs)
  (check-str "(* x (* xs))"))

(defexpect nested-addition-test
  '(+ x xs)
  (check-str "(+ x (+ xs))"))

(defexpect plus-0-test
  'x
  (check-str "(+ x 0)"))

(defexpect minus-0-test
  'x
  (check-str "(- x 0)"))

(defexpect multiply-by-1-test
  'x
  (check-str "(* x 1)"))

(defexpect divide-by-1-test
  'x
  (check-str "(/ x 1)"))

(defexpect multiply-by-0-test
  '0
  (check-str "(* x 0)"))

(defexpect conj-vec-test
  '(vector x)
  (check-str "(conj [] x)"))

(defexpect into-vec-test
  '(vec coll)
  (check-str "(into [] coll)"))

(defexpect assoc-assoc-key-coll-test
  '(assoc-in coll [:k0 :k1] v)
  (check-str "(assoc coll :k0 (assoc (:k0 coll) :k1 v))"))

(defexpect assoc-assoc-coll-key-test
  '(assoc-in coll [:k0 :k1] v)
  (check-str "(assoc coll :k0 (assoc (coll :k0) :k1 v))"))

(defexpect assoc-assoc-get-test
  '(assoc-in coll [:k0 :k1] v)
  (check-str "(assoc coll :k0 (assoc (get coll :k0) :k1 v))"))

(defexpect assoc-fn-key-coll-test
  '(update-in coll [:k] f args)
  (check-str "(assoc coll :k (f (:k coll) args))"))

(defexpect assoc-fn-coll-key-test
  '(update-in coll [:k] f args)
  (check-str "(assoc coll :k (f (coll :k) args))"))

(defexpect assoc-fn-get-test
  '(update-in coll [:k] f args)
  (check-str "(assoc coll :k (f (get coll :k) args))"))

(defexpect update-in-assoc-test
  '(assoc-in coll ks v)
  (check-str "(update-in coll ks assoc v)"))

(defexpect not-empty?-test
  '(not-empty x)
  (check-str "(not (empty? x))"))

(defexpect when-not-empty?-test
  (expect '(when (seq x) y) (check-str "(when-not (empty? x) y)"))
  (expect nil? (check-str "(if (= 1 called-with) \"arg\" \"args\")")))

(defexpect into-set-test
  '(set coll)
  (check-str "(into #{} coll)"))

(defexpect take-repeatedly-test
  '(repeatedly n coll)
  (check-str "(take n (repeatedly coll))"))

(defexpect dorun-map-test
  '(run! f coll)
  (check-str "(dorun (map f coll))"))

(defexpect if-else-nil-test
  (expect '(when x y) (check-str "(if x y nil)"))
  (expect nil? (check-str "(if x \"y\" \"z\")")))

(defexpect if-nil-else-test
  '(when-not x y)
  (check-str "(if x nil y)"))

(defexpect if-then-do-test
  '(when x y)
  (check-str "(if x (do y))"))

(defexpect if-not-x-y-x-test
  '(if-not x y z)
  (check-str "(if (not x) y z)"))

(defexpect if-x-x-y-test
  '(or x y)
  (check-str "(if x x y)"))

(defexpect when-not-x-y-test
  '(when-not x y)
  (check-str "(when (not x) y)"))

(defexpect useless-do-x-test
  'x
  (check-str "(do x)"))

(defexpect if-let-else-nil-test
  '(when-let ?binding ?expr)
  (check-str "(if-let ?binding ?expr nil)"))

(defexpect when-do-test
  '(when x y)
  (check-str "(when x (do y))"))

(defexpect when-not-do-test
  '(when-not x y)
  (check-str "(when-not x (do y))"))

(defexpect if-not-do-test
  '(when-not x y)
  (check-str "(if-not x (do y))"))

(defexpect if-not-not-test
  '(if x y z)
  (check-str "(if-not (not x) y z)"))

(defexpect when-not-not-test
  '(when x y)
  (check-str "(when-not (not x) y)"))

(defexpect loop-empty-when-test
  '(while (= 1 1) (prn 1) (prn 2))
  (check-str "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"))

(defexpect let-do-test
  '(let [a 1 b 2] (prn a b))
  (check-str "(let [a 1 b 2] (do (prn a b)))"))

(defexpect loop-do-test
  '(loop [] 1)
  (check-str "(loop [] (do 1))"))

(defexpect cond-else-test
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-str "(cond (pos? x) (inc x) :default -1)"))
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-str "(cond (pos? x) (inc x) true -1)"))
  (expect nil? (check-str "(cond (pos? x) (inc x) (neg? x) (dec x))"))
  (expect nil? (check-str "(cond :else true)")))

(defexpect not-eq-test
  '(not= arg1 arg2 arg3)
  (check-str "(not (= arg1 arg2 arg3))"))

(defexpect eq-0-x-test
  '(zero? x)
  (check-str "(= 0 x)"))

(defexpect eq-x-0-test
  '(zero? x)
  (check-str "(= x 0)"))

(defexpect eqeq-0-x-test
  '(zero? x)
  (check-str "(== 0 x)"))

(defexpect eqeq-x-0-test
  '(zero? x)
  (check-str "(== x 0)"))

(defexpect lt-0-x-test
  '(pos? x)
  (check-str "(< 0 x)"))

(defexpect lt-x-0-test
  '(neg? x)
  (check-str "(< x 0)"))

(defexpect gt-0-x-test
  '(neg? x)
  (check-str "(> 0 x)"))

(defexpect gt-x-0-test
  '(pos? x)
  (check-str "(> x 0)"))

(defexpect eq-true-test
  '(true? x)
  (check-str "(= true x)"))

(defexpect eq-false-test
  '(false? x)
  (check-str "(= false x)"))

(defexpect eq-x-nil-test
  '(nil? x)
  (check-str "(= x nil)"))

(defexpect eq-nil-x-test
  '(nil? x)
  (check-str "(= nil x)"))

(defexpect not-nil?-test
  '(some? x)
  (check-str "(not (nil? x))"))
