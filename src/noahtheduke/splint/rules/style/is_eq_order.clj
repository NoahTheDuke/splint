; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.is-eq-order
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn call-or-sym? [form]
  (or (symbol? form)
    (and (list? form)
      (not (#{'quote 'syntax-quote 'splint/syntax-quote} (first form))))))

(defrule style/is-eq-order
  "`clojure.test/is` expects `=`-based assertions to put the expected value first.

  This rule uses two checks on the `=` call to determine if it should issue a diagnostic:
  * Is the first argument a symbol or an unquoted list? (A variable/local or a call.)
  * Is the second argument a nil, boolean, char, number, keyword, or string?

  @examples

  ; avoid
  (is (= status 200))
  (is (= (my-plus 1 2) 3))

  ; prefer
  (is (= 200 status))
  (is (= 3 (my-plus 1 2)))

  ; non-issues
  (is (= (hash-map :a 1) {:a 1}))
  (is (= (hash-set :a 1) #{:a 1}))
  "
  {:pattern '(is (= (? actual call-or-sym?) (? expected simple-literal?)) ??msg)
   :message "Expected value should go first"
   :autocorrect true
   :replace '(is (= ?expected ?actual) ?msg)})
