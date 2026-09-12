; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.useless-destructure
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [defn-fn?? let?? for-doseq??]]))

(set! *warn-on-reflection* true)

(defn empty-destructure-binding
  [ctx rule bind expr]
  (->diagnostic ctx rule [bind expr]
    {:form-meta (meta bind)
     :message "Empty destructured binding"}))

(defn check-lets [ctx rule ?args]
  (when (even? (count ?args))
    (for [[bind expr] (partition 2 ?args)
          :when (#{[] {}} bind)]
      (empty-destructure-binding ctx rule bind expr))))

(defn check-defns [ctx rule form]
  (when-let [defn-form (:splint/defn-form (meta form))]
    (for [argvec (:arglists defn-form)
          arg argvec
          :when (#{[] {}} arg)]
      (->diagnostic ctx rule arg
                    {:message "Empty destructured parameter"}))))

(defn check-iterators [ctx rule ?args]
  (when (even? (count ?args))
    (let [diagnostics (volatile! [])]
      (doseq [[bind expr] (partition 2 ?args)]
        (cond
          (#{[] {}} bind)
          (vswap! diagnostics conj (empty-destructure-binding ctx rule bind expr))
          (and (= :let bind) (vector? expr))
          (doseq [[bind expr] (partition 2 expr)
                  :when (#{[] {}} bind)]
            (vswap! diagnostics conj (empty-destructure-binding ctx rule bind expr)))))
      (seq @diagnostics))))

(defrule lint/useless-destructure
  "Destructure targets can be empty, resulting in no variables bound. This usually indicates a bug or mistake in the binding.

  Checks the binding vectors of the following built-ins:
  * `let`, `loop`
  * `if-let`, `if-some`, `when-let`, `when-some`
  * `for`, `doseq`

  @examples

  ; avoid
  (let [{} (some-call)]
    ...)

  ; avoid
  (for [[] (range 5)]
    ...)

  ; prefer
  (let [foo (some-call)]
    ...)

  ; or if the variable is unused
  (let [_ (some-call)]
    ...)
  "
  {:patterns ['((? l let??) [?+args] ?*_)
              '((? d defn-fn??) ?*_)
              '((? f for-doseq??) [?+args] ?*_)]
   :on-match (fn [ctx rule form {:syms [?l ?d ?f ?args]}]
               (cond
                 ?l (check-lets ctx rule ?args)
                 ?d (check-defns ctx rule form)
                 ?f (check-iterators ctx rule ?args)))})
