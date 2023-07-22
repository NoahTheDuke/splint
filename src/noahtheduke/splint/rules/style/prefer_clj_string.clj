; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-clj-string
  (:require
    [noahtheduke.splint.pattern :as p]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.replace :refer [postwalk-splicing-replace]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn upper-case?? [sexp]
  (case (name sexp)
    (".toUpperCase" "upper-case") true
    false))

(defn lower-case?? [sexp]
  (case (name sexp)
    (".toLowerCase" "lower-case") true
    false))

(defn to-str?? [sexp]
  (case (name sexp)
    (".toString" "str") true
    false))

(def capitalize??
  {:pattern (p/pattern '((? _ to-str??)
                         ((? _ upper-case??) (subs ?s 0 1))
                         ((? _ lower-case??) (subs ?s 1))))
   :replace '(clojure.string/capitalize ?s)})

(def reverse??
  {:pattern (p/pattern '((? _ to-str??) (.reverse (StringBuilder. ?s))))
   :replace '(clojure.string/reverse ?s)})

(def interop->clj-string
  '{.contains clojure.string/includes?
    .endsWith clojure.string/ends-with?
    .replace clojure.string/replace
    .split clojure.string/split
    .startsWith clojure.string/starts-with?
    .toLowerCase clojure.string/lower-case
    ; TODO: Need a way to flag overlapping rules.
    #_#_.toString clojure.core/str
    .toUpperCase clojure.string/upper-case
    .trim clojure.string/trim})

(defrule style/prefer-clj-string
  "Prefer clojure.math to interop.

  Examples:

  # bad
  (.toUpperCase \"hello world\")

  # good
  (clojure.string/upper-case \"hello world\")
  "
  {:pattern '((? sym symbol?) ?*args)
   :message "Use the `clojure.string` function instead of interop."
   :on-match (fn [ctx rule form {:syms [?sym ?args]}]
               (if-let [binds ((:pattern capitalize??) form)]
                 (let [new-form (postwalk-splicing-replace binds (:replace capitalize??))]
                   (->diagnostic ctx rule form {:replace-form new-form}))
                 (if-let [binds ((:pattern reverse??) form)]
                   (let [new-form (postwalk-splicing-replace binds (:replace reverse??))]
                     (->diagnostic ctx rule form {:replace-form new-form}))
                   (when-let [replacement (interop->clj-string ?sym)]
                     (let [new-form (apply list replacement ?args)]
                       (->diagnostic ctx rule form {:replace-form new-form}))))))})
