; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-condp
  (:require
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn find-issue [?pairs]
  (when (and (even? (count ?pairs))
             (list? (first ?pairs))
             (= 3 (count (first ?pairs))))
    (let [all-pairs (partition 2 ?pairs)
          last-pred-f (first (last all-pairs))
          default? (or (keyword? last-pred-f)
                       (true? last-pred-f))]
      (when (if default?
              (< 2 (count all-pairs))
              (< 1 (count all-pairs)))
        (let [[test-expr] ?pairs
              [pred-f _ expr] test-expr
              ;; trim final pred if it's a keyword or `true`
              all-pairs (if default?
                          (butlast all-pairs)
                          all-pairs)]
          ;; Skip simple built-in macros
          (when-not (case pred-f (and or) true false)
            (when-let [test-exprs
                       (reduce
                         (fn [acc [cur-pred cur-branch]]
                           (if (and (list? cur-pred)
                                    (= pred-f (first cur-pred))
                                    (= expr (last cur-pred)))
                             (conj acc (second cur-pred) cur-branch)
                             (reduced nil)))
                         []
                         all-pairs)]
              (let [test-exprs (if default?
                                 (conj test-exprs (last ?pairs))
                                 test-exprs)]
                (list* 'condp pred-f expr test-exprs)))))))))

(defrule style/prefer-condp
  "`cond` checking against the same value in every branch is a code smell.

  This rule uses the first test-expr as the template to compare against each
  other test-expr. It has a number of conditions it checks as it runs:

  * The `cond` is well-formed (aka even number of args).
  * The `cond` has more than 1 pair.
  * The first test-expr is a list with 3 forms.
  * The function of every test-expr must match the test-expr of the first
    test-expr.
    * The last test-expr isn't checked if it is `true` or a keyword.
  * The last argument of every test-expr must match the last argument of the
    first test-expr.

  Provided all of that is true, then the middle arguments of the test-exprs are
  gathered and rendered into a `condp`.

  Examples:

  # bad
  (cond
    (= 1 x) :one
    (= 2 x) :two
    (= 3 x) :three
    (= 4 x) :four)

  # good
  (condp = x
    1 :one
    2 :two
    3 :three
    4 :four)

  # bad
  (cond
    (= 1 x) :one
    (= 2 x) :two
    (= 3 x) :three
    :else :big)

  # good
  (condp = x
    1 :one
    2 :two
    3 :three
    :big)
  "
  {:pattern2 '(cond ?*pairs)
   :on-match (fn [ctx rule form {:syms [?pairs]}]
               (when-let [new-form (find-issue ?pairs)]
                 (let [message "Prefer condp when predicate and arguments are the same"]
                   (->diagnostic ctx rule form {:replace-form new-form
                                                :message message}))))})
