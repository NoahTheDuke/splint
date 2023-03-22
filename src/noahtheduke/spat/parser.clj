; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser
  (:require
    [edamame.core :as e]
    [edamame.impl.read-fn :as read-fn]))

(set! *warn-on-reflection* true)

(defn make-edamame-opts []
  {:all true
   :row-key :line
   :col-key :column
   :end-location true
   :features #{:cljs}
   :read-cond :preserve
   :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "splint-auto" (name r))) v)))
   :auto-resolve-ns true
   :auto-resolve (fn [k] (if (= :current k) "splint-auto_" (str "splint-auto_" (name k))))
   :uneval (fn [{:keys [uneval next]}]
             (cond
               (identical? uneval :splint/disable)
               (with-meta next {:splint/disable true})
               (and (seqable? (:splint/disable uneval))
                    (seq (:splint/disable uneval)))
               (with-meta next {:splint/disable (seq (:splint/disable uneval))})
               :else
               next))
   ; All reader macros should be a splint-specific symbol wrapping the expression
   :dispatch {; @x
              \@ (fn [expr] (list 'splint/deref expr))
              ; `(+ 1 2)
              ; This is a deliberate decision to not expand syntax quotes. Easier to parse/manipulate the entire tree.
              \` (fn [expr] (list 'splint/syntax-quote expr))
              \~ {; ~x unquote
                  :default (fn [expr] (list 'splint/unquote expr))
                  ; ~@(map inc [1 2 3])
                  \@ (fn [expr] (list 'splint/unquote-splicing expr))}
              \# {; #'x
                  \' (fn [expr] (list 'splint/var expr))
                  ; #=(+ 1 2)
                  \= (fn [expr] (list 'splint/read-eval expr))
                  ; #()
                  \( (fn [expr]
                       (let [sexp (read-fn/read-fn expr)]
                         (apply list (cons 'splint/fn (next sexp)))))
                  ; #".*"
                  \" (fn [expr] (list 'splint/re-pattern expr))}}})

(defn parse-string [s] (e/parse-string s (make-edamame-opts)))
(defn parse-string-all [s] (e/parse-string-all s (make-edamame-opts)))
