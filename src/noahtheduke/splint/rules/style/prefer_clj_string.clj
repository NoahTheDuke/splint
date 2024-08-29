; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-clj-string
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [interop->clj-string]]))

(set! *warn-on-reflection* true)

(defn upper-case?? [sexp]
  (and (symbol? sexp)
    (#{".toUpperCase" "upper-case"} (name sexp))))

(defn lower-case?? [sexp]
  (and (symbol? sexp)
    (#{".toLowerCase" "lower-case"} (name sexp))))

(defn to-str?? [sexp]
  (and (symbol? sexp)
    (#{".toString" "str"} (name sexp))))

(defrule style/prefer-clj-string
  "Prefer clojure.string to interop.

  | method | clojure.string |
  | --- | --- |
  | `.contains` | `clojure.string/includes?` |
  | `.endsWith` | `clojure.string/ends-with?` |
  | `.replace` | `clojure.string/replace` |
  | `.split` | `clojure.string/split` |
  | `.startsWith` | `clojure.string/starts-with?` |
  | `.toLowerCase` | `clojure.string/lower-case` |
  | `.toUpperCase` | `clojure.string/upper-case` |
  | `.trim` | `clojure.string/trim` |

  @examples

  ; avoid
  (.toUpperCase \"hello world\")

  ; prefer
  (clojure.string/upper-case \"hello world\")
  "
  {:patterns ['((? _ to-str??)
                ((? _ upper-case??) (subs ?cap 0 1))
                ((? _ lower-case??) (subs ?cap 1)))
              '((? _ to-str??) (.reverse (StringBuilder. ?rev)))
              '((? plain string-interop-method?) ?*args)]
   :message "Use the `clojure.string` function instead of interop."
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?cap ?rev ?plain ?args]}]
               (cond
                 ?cap
                 (let [new-form (list 'clojure.string/capitalize ?cap)]
                   (->diagnostic ctx rule form {:replace-form new-form}))
                 ?rev
                 (let [new-form (list 'clojure.string/reverse ?rev)]
                   (->diagnostic ctx rule form {:replace-form new-form}))
                 :else
                 (when-let [replacement (interop->clj-string ?plain)]
                   (let [new-form (apply list replacement ?args)]
                     (->diagnostic ctx rule form {:replace-form new-form})))))})
