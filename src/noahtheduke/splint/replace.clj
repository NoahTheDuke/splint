; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace
  (:require
    [clojure.walk :as walk]))

(set! *warn-on-reflection* true)

(defn- render-deref [[_ sexp]]
  (list `deref sexp))

(defn- render-fn [sexp]
  (apply list (cons `fn (next sexp))))

(defn- render-read-eval [sexp]
  (with-meta (apply list (cons (symbol "#=") (next sexp)))
             {::uplift true}))

(defn- render-re-pattern [[_ sexp]]
  (list `re-pattern sexp))

(defn- render-var [[_ sexp]]
  (list `var sexp))

(defn- render-syntax-quote [sexp]
  (if (symbol? (second sexp))
    (symbol (str "`" (second sexp)))
    (with-meta (apply list (cons (symbol "`") (next sexp)))
               {::uplift true})))

(defn- render-unquote [[_ sexp]]
  (list `unquote sexp))

(defn- render-unquote-splicing [[_ sexp]]
  (list `unquote-splicing sexp))

(defn- revert-splint-reader-macros [sexp]
  (if-let [f (case (first sexp)
               splint/deref render-deref
               splint/fn render-fn
               splint/read-eval render-read-eval
               splint/re-pattern render-re-pattern
               splint/var render-var
               splint/syntax-quote render-syntax-quote
               splint/unquote render-unquote
               splint/unquote-splicing render-unquote-splicing
               ; else
               nil)]
    (f sexp)
    sexp))

(defn- uplift [sexp]
  (if (symbol? sexp)
    sexp
    (->> (reduce
           (fn [acc cur]
             (if (::uplift (meta cur))
               (apply conj acc cur)
               (conj acc cur)))
           []
           sexp)
         (apply list))))

(defn- splicing-replace [sexp]
  (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) sexp)]
    (concat front-sexp (second rest-sexp) (drop 2 rest-sexp))))

(defn postwalk-splicing-replace [binds replace-form]
  (walk/postwalk
    (fn [item]
      (cond
        (seq? item) (-> item
                        (splicing-replace)
                        (revert-splint-reader-macros)
                        (uplift))
        (contains? binds item) (binds item)
        :else
        item))
    replace-form))
