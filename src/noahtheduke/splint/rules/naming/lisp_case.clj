; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.lisp-case
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn def*?? [sexp]
  (and (symbol? sexp)
    (#{"def" "defn"} (name sexp))))

(defn incorrect-name? [sexp]
  (when (symbol? sexp)
    (let [def*-name (str sexp)]
      (and (some? (re-find #"(._.|[a-z][A-Z])" def*-name))
        (not (str/includes? def*-name "->"))
        (not (str/ends-with? def*-name "?"))))))

(defrule naming/lisp-case
  "Use lisp-case for function and variable names. (Replacement is generated with [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).)

  Skips names that contain `->` or end in `?`, which indicate conversion functions or type predicates, respectfully.

  @safety
  Interop, json, and other styles can make it necessary to use such forms.

  @examples

  ; avoid
  (def someVar ...)
  (def some_fun ...)

  ; prefer
  (def some-var ...)
  (defn some-fun ...)

  ; ignores
  (defn StackTraceElement->vec [o] ...)
  (defn NaN? [n] ...)
  "
  {:pattern '((? def def*??) (? name incorrect-name?) ?*args)
   :message "Prefer kebab-case over other cases for top-level definitions."
   :on-match (fn [ctx rule form {:syms [?def ?name ?args]}]
               (when (nil? (:parent-form ctx))
                 (let [new-form (list* ?def (csk/->kebab-case-symbol ?name) ?args)]
                   (->diagnostic ctx rule form {:replace-form new-form}))))})
