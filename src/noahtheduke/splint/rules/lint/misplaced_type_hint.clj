; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.misplaced-type-hint
  (:require
   [noahtheduke.splint.clojure-ext.core :refer [->list]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/misplaced-type-hint
  "In interop scenarios, it can be necessary to add a type hint to mark a function's return type. This can be done by adding metadata to the function's name symbol or to the function's param vector. The former works but is prone to errors and is not recommended by the core team, whereas the latter is the official method. (See links below for further discussion.)

  @note
  Only checks `defn` forms. (Compare with [eastwood's `:wrong-tag`](https://github.com/jonase/eastwood#wrong-tag) linter.)

  @examples

  ; avoid
  (defn ^String make-str
    []
    \"abc\")

  (defn ^String make-str
    ([] \"abc\")
    ([a] (str a \"abc\")))

  ; prefer
  (defn make-str ^String [] \"abc\")

  (defn make-str
    (^String [] \"abc\")
    (^String [a] (str a \"abc\")))
  "
  {:pattern '((? defn defn??) ?*args)
   :ext :clj
   :on-match
   (fn [ctx rule form {:syms [?defn]}]
     (when-let [defn-form (:splint/defn-form (meta form))]
       (when-let [tag (:tag defn-form)]
         (let [arities (:arities defn-form)
               tag (symbol (str "^" tag))
               arities (if (= 1 (count (:arities defn-form)))
                         (cons tag (first arities))
                         (map #(cons tag %) arities))
               replace-form (->list (list* ?defn (:splint/name defn-form) arities))
               original-form (with-meta (->list (list* ?defn tag (next form)))
                               (meta form))]
           (->diagnostic ctx rule original-form {:replace-form replace-form})))))})
