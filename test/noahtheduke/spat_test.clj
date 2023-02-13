; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat-test
  (:require [expectations.clojure.test :refer [defexpect]]
            [clj-kondo.impl.rewrite-clj.parser :as p]
            [noahtheduke.spat :as spat]))

(set! *warn-on-reflection* true)

(defn check-str
  [s]
  (spat/check-all-rules (p/parse-string s)))

#_(do (doall (for [rule-sym [`spat/string-rules
                `spat/sequence-rules
                `spat/first-next-rules
                `spat/fn-rules
                `spat/threading-rules
                `spat/misc-rules
                `spat/math-rules
                `spat/coll-rules
                `spat/control-flow-rules
                `spat/equality-rules]
      :let [rules @(requiring-resolve rule-sym)]
      rule rules
      :let [{:keys [name pattern-raw replace-raw]} rule]]
  (do (printf
    "(defexpect %s%n  '%s%n  (check-str \"%s\"))%n%n"
    (str name "-test")
    (fnext replace-raw) (fnext pattern-raw)
    )
      (flush))))
    nil)

(defexpect str-to-string-test
  '(str ?x)
  (check-str "(.toString ?x)"))

(defexpect str-apply-interpose-test
  '(clojure.string/join ?x ?y)
  (check-str "(apply str (interpose ?x ?y))"))

(defexpect str-apply-reverse-test
  '(clojure.string/reverse ?x)
  (check-str "(apply str (reverse ?x))"))

(defexpect str-apply-str-test
  '(clojure.string/join ?x)
  (check-str "(apply str ?x)"))

(defexpect mapcat-apply-apply-test
  '(mapcat ?x ?y)
  (check-str "(apply concat (apply map ?x ?y))"))

(defexpect mapcat-concat-map-test
  '(mapcat ?x . ?y)
  (check-str "(apply concat (map ?x . ?y))"))

(defexpect filter-complement-test
  '(remove ?pred ?coll)
  (check-str "(filter (complement ?pred) ?coll)"))

(defexpect filter-seq-test
  '(remove empty? ?coll)
  (check-str "(filter seq ?coll)"))

(defexpect filter-fn*-not-pred-test
  '(remove ?pred ?coll)
  (check-str "(filter (fn* [?x] (not (?pred ?x))) ?coll)"))

(defexpect filter-fn-not-pred-test
  '(remove ?pred ?coll)
  (check-str "(filter (fn [?x] (not (?pred ?x))) ?coll)"))

(defexpect filter-vec-filter-test
  '(filterv ?pred ?coll)
  (check-str "(vec (filter ?pred ?coll))"))

(defexpect first-first-test
  '(ffirst ?coll)
  (check-str "(first (first ?coll))"))

(defexpect first-next-test
  '(fnext ?coll)
  (check-str "(first (next ?coll))"))

(defexpect next-first-test
  '(nfirst ?coll)
  (check-str "(next (first ?coll))"))

(defexpect next-next-test
  '(nnext ?coll)
  (check-str "(next (next ?coll))"))

(defexpect fn*-wrapper-test
  '?fun
  (check-str "(fn* [?arg] (?fun ?arg))"))

(defexpect fn-wrapper-test
  '?fun
  (check-str "(fn [?arg] (?fun ?arg))"))

(defexpect thread-first-no-arg-test
  '?x
  (check-str "(-> ?x)"))

(defexpect thread-first-1-arg-test
  'null
  (check-str "(-> ?arg %symbol-or-keyword-or-list?%-?form)"))

(defexpect thread-last-no-arg-test
  '?x
  (check-str "(->> ?x)"))

(defexpect thread-last-1-arg-test
  'null
  (check-str "(->> ?arg %symbol-or-keyword-or-list?%-?form)"))

(defexpect not-some-pred-test
  '(not-any? ?pred ?coll)
  (check-str "(not (some ?pred ?coll))"))

(defexpect with-meta-f-meta-test
  'null
  (check-str "(with-meta ?x (?f (meta ?x) &&. ?args))"))

(defexpect plus-x-1-test
  '(inc ?x)
  (check-str "(+ ?x 1)"))

(defexpect plus-1-x-test
  '(inc ?x)
  (check-str "(+ 1 ?x)"))

(defexpect minus-x-1-test
  '(dec ?x)
  (check-str "(- ?x 1)"))

(defexpect nested-muliply-test
  '(* ?x &&. ?xs)
  (check-str "(* ?x (* &&. ?xs))"))

(defexpect nested-addition-test
  '(+ ?x &&. ?xs)
  (check-str "(+ ?x (+ &&. ?xs))"))

(defexpect plus-0-test
  '?x
  (check-str "(+ ?x 0)"))

(defexpect minus-0-test
  '?x
  (check-str "(- ?x 0)"))

(defexpect multiply-by-1-test
  '?x
  (check-str "(* ?x 1)"))

(defexpect divide-by-1-test
  '?x
  (check-str "(/ ?x 1)"))

(defexpect multiply-by-0-test
  '0
  (check-str "(* ?x 0)"))

(defexpect conj-vec-test
  '(vector &&. ?x)
  (check-str "(conj [] &&. ?x)"))

(defexpect into-vec-test
  '(vec ?coll)
  (check-str "(into [] ?coll)"))

(defexpect assoc-assoc-key-coll-test
  '(assoc-in ?coll [?key0 ?key1] ?val)
  (check-str "(assoc ?coll ?key0 (assoc (?key0 ?coll) ?key1 ?val))"))

(defexpect assoc-assoc-coll-key-test
  '(assoc-in ?coll [?key0 ?key1] ?val)
  (check-str "(assoc ?coll ?key0 (assoc (?coll ?key0) ?key1 ?val))"))

(defexpect assoc-assoc-get-test
  '(assoc-in ?coll [?key0 ?key1] ?val)
  (check-str "(assoc ?coll ?key0 (assoc (get ?coll ?key0) ?key1 ?val))"))

(defexpect assoc-fn-key-coll-test
  '(update-in ?coll [?key] ?fn &&. ?args)
  (check-str "(assoc ?coll ?key (?fn (?key ?coll) &&. ?args))"))

(defexpect assoc-fn-coll-key-test
  '(update-in ?coll [?key] ?fn &&. ?args)
  (check-str "(assoc ?coll ?key (?fn (?coll ?key) &&. ?args))"))

(defexpect assoc-fn-get-test
  '(update-in ?coll [?key] ?fn &&. ?args)
  (check-str "(assoc ?coll ?key (?fn (get ?coll ?key) &&. ?args))"))

(defexpect update-in-assoc-test
  '(assoc-in ?coll ?keys ?val)
  (check-str "(update-in ?coll ?keys assoc ?val)"))

(defexpect not-empty?-test
  '(seq ?x)
  (check-str "(not (empty? ?x))"))

(defexpect when-not-empty?-test
  '(when (seq ?x) &&. ?y)
  (check-str "(when-not (empty? ?x) &&. ?y)"))

(defexpect into-set-test
  '(set ?coll)
  (check-str "(into #{} ?coll)"))

(defexpect take-repeatedly-test
  '(repeatedly ?n ?coll)
  (check-str "(take ?n (repeatedly ?coll))"))

(defexpect dorun-map-test
  '(run! ?fn ?coll)
  (check-str "(dorun (map ?fn ?coll))"))

(defexpect if-else-nil-test
  '(when ?x ?y)
  (check-str "(if ?x ?y nil)"))

(defexpect if-nil-else-test
  '(when-not ?x ?y)
  (check-str "(if ?x nil ?y)"))

(defexpect if-then-do-test
  '(when ?x &&. ?y)
  (check-str "(if ?x (do &&. ?y))"))

(defexpect if-not-x-y-x-test
  '(if-not ?x ?y ?z)
  (check-str "(if (not ?x) ?y ?z)"))

(defexpect if-x-x-y-test
  '(or ?x ?y)
  (check-str "(if ?x ?x ?y)"))

(defexpect when-not-x-y-test
  '(when-not ?x &&. ?y)
  (check-str "(when (not ?x) &&. ?y)"))

(defexpect do-x-test
  '?x
  (check-str "(do ?x)"))

(defexpect if-let-else-nil-test
  '(when-let ?binding ?expr)
  (check-str "(if-let ?binding ?expr nil)"))

(defexpect when-do-test
  '(when ?x &&. ?y)
  (check-str "(when ?x (do &&. ?y))"))

(defexpect when-not-do-test
  '(when-not ?x &&. ?y)
  (check-str "(when-not ?x (do &&. ?y))"))

(defexpect if-not-do-test
  '(when-not ?x &&. ?y)
  (check-str "(if-not ?x (do &&. ?y))"))

(defexpect if-not-not-test
  '(if ?x ?y ?z)
  (check-str "(if-not (not ?x) ?y ?z)"))

(defexpect when-not-not-test
  '(when ?x &&. ?y)
  (check-str "(when-not (not ?x) &&. ?y)"))

(defexpect loop-empty-when-test
  '(while ?test &&. ?exprs)
  (check-str "(loop [] (when ?test &&. ?exprs (recur)))"))

(defexpect let-do-test
  '(let ?binding &&. ?exprs)
  (check-str "(let ?binding (do &&. ?exprs))"))

(defexpect loop-do-test
  '(loop [] 1)
  (check-str "(loop [] (do 1))"))

(defexpect not-eq-test
  '(not= &&. ?args)
  (check-str "(not (= &&. ?args))"))

(defexpect eq-0-x-test
  '(zero? ?x)
  (check-str "(= 0 ?x)"))

(defexpect eq-x-0-test
  '(zero? ?x)
  (check-str "(= ?x 0)"))

(defexpect eqeq-0-x-test
  '(zero? ?x)
  (check-str "(== 0 ?x)"))

(defexpect eqeq-x-0-test
  '(zero? ?x)
  (check-str "(== ?x 0)"))

(defexpect lt-0-x-test
  '(pos? ?x)
  (check-str "(< 0 ?x)"))

(defexpect lt-x-0-test
  '(neg? ?x)
  (check-str "(< ?x 0)"))

(defexpect gt-0-x-test
  '(neg? ?x)
  (check-str "(> 0 ?x)"))

(defexpect gt-x-0-test
  '(pos? ?x)
  (check-str "(> ?x 0)"))

(defexpect eq-true-test
  '(true? ?x)
  (check-str "(= true ?x)"))

(defexpect eq-false-test
  '(false? ?x)
  (check-str "(= false ?x)"))

(defexpect eq-x-nil-test
  '(nil? ?x)
  (check-str "(= ?x nil)"))

(defexpect eq-nil-x-test
  '(nil? x)
  (check-str "(= nil x)"))

(defexpect not-nil?-test
  '(some? ?x)
  (check-str "(not (nil? ?x))"))
