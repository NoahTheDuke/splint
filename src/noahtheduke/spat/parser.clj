; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser
  (:require
    [edamame.core :as e]
    [edamame.impl.read-fn :as read-fn]))

(set! *warn-on-reflection* true)

(def clj-defaults
  {:all true
   :row-key :line
   :col-key :column
   :end-location true
   :location? seq?
   :features #{:cljs}
   :read-cond :preserve
   :auto-resolve-ns true
   :auto-resolve (fn [k] (if (= :current k) "splint-auto_" (str "splint-auto_" (name k))))
   :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "splint-auto" (name r))) v)))
   ; All reader macros should be a splint-specific symbol wrapping the expression
   :dispatch {; @x
              \@ (fn [expr] (list 'splint/deref expr))
              ; `(+ 1 2)
              \` (fn [expr] (list 'splint/syntax-quote expr))
              \~ {; ~x unquote
                  :default (fn [expr] (list 'splint/unquote expr))
                  ; ~@(map inc [1 2 3])
                  \@ (fn [expr] (list 'splint/unquote-splice expr))}
              \# {; #'x
                  \' (fn [expr] (list 'splint/var expr))
                  ; #=(+ 1 2)
                  \= (fn [expr] (list 'splint/read-eval expr))
                  \_ (fn [_expr] :splint/ignore)
                  ; #()
                  \( (fn [expr]
                       (let [sexp (read-fn/read-fn expr)]
                         (apply list (cons 'splint/fn (next sexp)))))
                  ; #".*"
                  \" (fn [expr] (list 'splint/regex expr))}}})

(defn parse-string [s] (e/parse-string s clj-defaults))
(defn parse-string-all [s] (e/parse-string-all s clj-defaults))
