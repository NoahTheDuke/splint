; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.utils)

(set! *warn-on-reflection* true)

(defn drop-quote
  "Convert (quote (a b c)) to (a b c)."
  [sexp]
  (if (and (seq? sexp)
           (= 'quote (first sexp)))
    (fnext sexp)
    sexp))

(defprotocol SexpType
  (simple-type
    [sexp]
    "Because Clojure doesn't have this built-in, we must do it the slow way: take
    an object and return a keyword representing that object.

    ```clojure
    nil -> :nil
    true/false -> :boolean
    \\c -> :char
    1 -> :number
    :hello -> :keyword
    \"hello\" -> :string
    hello -> :symbol
    {:a :b} -> :map
    #{:a :b} -> :set
    [:a :b] -> :vector
    (1 2 3) -> :list
    :else -> (type sexp)
    ```"))

(extend-protocol SexpType
  ; literals
  nil (simple-type [_sexp] :nil)
  Boolean (simple-type [_sexp] :boolean)
  Character (simple-type [_sexp] :char)
  Number (simple-type [_sexp] :number)
  String (simple-type [_sexp] :string)
  clojure.lang.Keyword (simple-type [_sexp] :keyword)
  clojure.lang.Symbol (simple-type [_sexp] :symbol)
  ; reader macros
  clojure.lang.IPersistentMap (simple-type [_sexp] :map)
  clojure.lang.IPersistentSet (simple-type [_sexp] :set)
  clojure.lang.IPersistentVector (simple-type [_sexp] :vector)
  clojure.lang.ISeq (simple-type [_sexp] :list)
  ; else
  Object (simple-type [sexp] (symbol (pr-str (type sexp)))))

(comment
  (simple-type {:a 1})
  (simple-type (Object.))
  (simple-type `(1 2 3)))
