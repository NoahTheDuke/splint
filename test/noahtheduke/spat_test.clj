; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [noahtheduke.spat]
            [noahtheduke.spat.pattern :refer [pattern]]
            [noahtheduke.spat.rules :refer [global-rules]]
            [noahtheduke.spat.runner :refer [parse-string check-form]]
            [noahtheduke.spat.config :refer [load-config]]))

(set! *warn-on-reflection* true)

(def config (load-config))

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
    (check-form ctx config @global-rules form)))

(defn check-alt
  [s]
  (:alt (first (check-str s))))

(defexpect str-to-string-test
  '(str x)
  (check-alt "(.toString x)"))

(defexpect dot-obj-usage-test
  '(.method obj 1 2 3)
  (check-alt "(. obj method 1 2 3)"))

(defexpect dot-class-usage-test
  '(Obj/method 1 2 3)
  (check-alt "(. Obj method 1 2 3)"))

(defexpect str-apply-interpose-test
  '(clojure.string/join x y)
  (check-alt "(apply str (interpose x y))"))

(defexpect str-apply-reverse-test
  '(clojure.string/reverse x)
  (check-alt "(apply str (reverse x))"))

(defexpect str-apply-str-test
  '(clojure.string/join x)
  (check-alt "(apply str x)"))

(defexpect mapcat-apply-apply-test
  '(mapcat x y)
  (check-alt "(apply concat (apply map x y))"))

(defexpect mapcat-concat-map-test
  '(mapcat x y z)
  (check-alt "(apply concat (map x y z))"))

(defexpect filter-complement-test
  '(remove pred coll)
  (check-alt "(filter (complement pred) coll)"))

(defexpect filter-not-pred-test
  '(remove pred coll)
  (check-alt "(filter #(not (pred %)) coll)"))

(defexpect filter-fn*-not-pred-test
  '(remove pred coll)
  (check-alt "(filter (fn* [x] (not (pred x))) coll)"))

(defexpect filter-fn-not-pred-test
  '(remove pred coll)
  (check-alt "(filter (fn [x] (not (pred x))) coll)"))

(defexpect filter-vec-filter-test
  '(filterv pred coll)
  (check-alt "(vec (filter pred coll))"))

(defexpect first-first-test
  '(ffirst coll)
  (check-alt "(first (first coll))"))

(defexpect first-next-test
  '(fnext coll)
  (check-alt "(first (next coll))"))

(defexpect next-first-test
  '(nfirst coll)
  (check-alt "(next (first coll))"))

(defexpect next-next-test
  '(nnext coll)
  (check-alt "(next (next coll))"))

(defexpect fn*-wrapper-test
  'f
  (check-alt "(fn* [arg] (f arg))"))

(defexpect fn-wrapper-test
  'f
  (check-alt "(fn [arg] (f arg))"))

(defexpect thread-first-no-arg-test
  'x
  (check-alt "(-> x)"))

(defexpect thread-first-1-arg-test
  (expect '(f arg) (check-alt "(-> arg f)"))
  (expect '(f arg) (check-alt "(-> arg (f))")))

(defexpect thread-last-no-arg-test
  'x
  (check-alt "(->> x)"))

(defexpect thread-last-1-arg-test
  (expect '(form arg) (check-alt "(->> arg form)"))
  (expect '(form arg) (check-alt "(->> arg (form))")))

(defexpect not-some-pred-test
  '(not-any? pred coll)
  (check-alt "(not (some pred coll))"))

(defexpect with-meta-f-meta-test
  '(vary-meta x f args)
  (check-alt "(with-meta x (f (meta x) args))"))

(defexpect plus-x-1-test
  '(inc x)
  (check-alt "(+ x 1)"))

(defexpect plus-1-x-test
  '(inc x)
  (check-alt "(+ 1 x)"))

(defexpect minus-x-1-test
  '(dec x)
  (check-alt "(- x 1)"))

(defexpect nested-muliply-test
  '(* x xs)
  (check-alt "(* x (* xs))"))

(defexpect nested-addition-test
  '(+ x xs)
  (check-alt "(+ x (+ xs))"))

(defexpect plus-0-test
  'x
  (check-alt "(+ x 0)"))

(defexpect minus-0-test
  'x
  (check-alt "(- x 0)"))

(defexpect multiply-by-1-test
  'x
  (check-alt "(* x 1)"))

(defexpect divide-by-1-test
  'x
  (check-alt "(/ x 1)"))

(defexpect multiply-by-0-test
  '0
  (check-alt "(* x 0)"))

(defexpect conj-vec-test
  '(vector x)
  (check-alt "(conj [] x)"))

(defexpect into-vec-test
  '(vec coll)
  (check-alt "(into [] coll)"))

(defexpect assoc-in-one-arg-test
  '(assoc coll :k v)
  (check-alt "(assoc-in coll [:k] v)"))

(defexpect assoc-assoc-key-coll-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (:k1 coll) :k2 v))"))

(defexpect assoc-assoc-coll-key-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (coll :k1) :k2 v))"))

(defexpect assoc-assoc-get-test
  '(assoc-in coll [:k1 :k2] v)
  (check-alt "(assoc coll :k1 (assoc (get coll :k1) :k2 v))"))

(defexpect assoc-fn-key-coll-test
  '(update coll :k f args)
  (check-alt "(assoc coll :k (f (:k coll) args))"))

(defexpect assoc-fn-coll-key-test
  '(update coll :k f args)
  (check-alt "(assoc coll :k (f (coll :k) args))"))

(defexpect assoc-fn-get-test
  '(update coll :k f args)
  (check-alt "(assoc coll :k (f (get coll :k) args))"))

(defexpect update-in-one-arg-test
  (expect '(update coll :k inc) (check-alt "(update-in coll [:k] inc)"))
  (expect '(update coll :k + 1 2 3) (check-alt "(update-in coll [:k] + 1 2 3)")))

(defexpect update-in-assoc-test
  '(assoc-in coll ks v)
  (check-alt "(update-in coll ks assoc v)"))

(defexpect not-empty?-test
  '(seq x)
  (check-alt "(not (empty? x))"))

(defexpect when-not-empty?-test
  (expect '(when (seq x) y) (check-alt "(when-not (empty? x) y)"))
  (expect nil? (check-alt "(if (= 1 called-with) \"arg\" \"args\")")))

(defexpect into-set-test
  '(set coll)
  (check-alt "(into #{} coll)"))

(defexpect take-repeatedly-test
  '(repeatedly n coll)
  (check-alt "(take n (repeatedly coll))"))

(defexpect dorun-map-test
  '(run! f coll)
  (check-alt "(dorun (map f coll))"))

(defexpect if-else-nil-test
  (expect '(when x y) (check-alt "(if x y nil)"))
  (expect nil? (check-alt "(if x \"y\" \"z\")")))

(defexpect if-nil-else-test
  '(when-not x y)
  (check-alt "(if x nil y)"))

(defexpect if-then-do-test
  '(when x y)
  (check-alt "(if x (do y))"))

(defexpect if-not-x-y-x-test
  '(if-not x y z)
  (check-alt "(if (not x) y z)"))

(defexpect if-x-x-y-test
  '(or x y)
  (check-alt "(if x x y)"))

(defexpect when-not-x-y-test
  '(when-not x y)
  (check-alt "(when (not x) y)"))

(defexpect useless-do-x-test
  'x
  (check-alt "(do x)"))

(defexpect if-let-else-nil-test
  '(when-let ?binding ?expr)
  (check-alt "(if-let ?binding ?expr nil)"))

(defexpect when-do-test
  '(when x y)
  (check-alt "(when x (do y))"))

(defexpect let-when-test
  '(when-let [result (some-func)] (do-stuff result))
  (check-alt "(let [result (some-func)] (when result (do-stuff result)))"))

(defexpect let-if-test
  '(if-let [result (some-func)] (do-stuff result) (other-stuff))
  (check-alt "(let [result (some-func)] (if result (do-stuff result) (other-stuff)))"))

(defexpect when-not-do-test
  '(when-not x y)
  (check-alt "(when-not x (do y))"))

(defexpect if-not-do-test
  '(when-not x y)
  (check-alt "(if-not x (do y))"))

(defexpect if-not-not-test
  '(if x y z)
  (check-alt "(if-not (not x) y z)"))

(defexpect when-not-not-test
  '(when x y)
  (check-alt "(when-not (not x) y)"))

(defexpect loop-empty-when-test
  '(while (= 1 1) (prn 1) (prn 2))
  (check-alt "(loop [] (when (= 1 1) (prn 1) (prn 2) (recur)))"))

(defexpect let-do-test
  '(let [a 1 b 2] (prn a b))
  (check-alt "(let [a 1 b 2] (do (prn a b)))"))

(defexpect loop-do-test
  '(loop [] 1)
  (check-alt "(loop [] (do 1))"))

(defexpect cond-else-test
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-alt "(cond (pos? x) (inc x) :default -1)"))
  (expect '(cond (pos? x) (inc x) :else -1)
    (check-alt "(cond (pos? x) (inc x) true -1)"))
  (expect nil? (check-alt "(cond (pos? x) (inc x) (neg? x) (dec x))"))
  (expect nil? (check-alt "(cond :else true)")))

(defexpect not-eq-test
  '(not= arg1 arg2 arg3)
  (check-alt "(not (= arg1 arg2 arg3))"))

(defexpect eq-0-x-test
  '(zero? x)
  (check-alt "(= 0 x)"))

(defexpect eq-x-0-test
  '(zero? x)
  (check-alt "(= x 0)"))

(defexpect eqeq-0-x-test
  '(zero? x)
  (check-alt "(== 0 x)"))

(defexpect eqeq-x-0-test
  '(zero? x)
  (check-alt "(== x 0)"))

(defexpect lt-0-x-test
  '(pos? x)
  (check-alt "(< 0 x)"))

(defexpect lt-x-0-test
  '(neg? x)
  (check-alt "(< x 0)"))

(defexpect gt-0-x-test
  '(neg? x)
  (check-alt "(> 0 x)"))

(defexpect gt-x-0-test
  '(pos? x)
  (check-alt "(> x 0)"))

(defexpect eq-true-test
  '(true? x)
  (check-alt "(= true x)"))

(defexpect eq-false-test
  '(false? x)
  (check-alt "(= false x)"))

(defexpect eq-x-nil-test
  '(nil? x)
  (check-alt "(= x nil)"))

(defexpect eq-nil-x-test
  '(nil? x)
  (check-alt "(= nil x)"))

(defexpect not-nil?-test
  '(some? x)
  (check-alt "(not (nil? x))"))

(defexpect missing-body-in-when-test
  "Missing body in when"
  (:message (first (check-str "(when true)"))))

(defexpect new-object-test
  '(java.util.ArrayList. 100)
  (check-alt "(new java.util.ArrayList 100)"))
