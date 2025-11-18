; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.redundant-str-call
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn str?? [sexp]
  (and (symbol? sexp)
    (String/.equals "str" (name sexp))))

(defn nested?? [sexp]
  (and (symbol? sexp)
    (#{"format" "str"} (name sexp))))

(defrule lint/redundant-str-call
  "`clojure.core/str` calls `.toString()` on non-nil input. However, `.toString()` on a string literal returns itself, making it a no-op. Likewise, `clojure.core/format` unconditionally returns a string, making any calls to `str` on the results a no-op.

  @examples

  ; avoid
  (str \"foo\")
  (str (format \"foo-%s\" some-var))

  ; prefer
  \"foo\"
  (format \"foo-%s\" some-var)
  "
  {:patterns ['((? fn str??) (? literal string?))
              '((? fn str??) ((? nested nested??) ?*args))]
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?fn ?literal ?nested ?args]}]
               (let [parent-form (:parent-form ctx)]
                 (when-not (and (seq? parent-form)
                                (#{'-> '->> 'cond-> 'cond->> 'some-> 'some->>} (first parent-form)))
                   (let [new-form (if ?nested
                                    (list* ?nested ?args)
                                    ?literal)
                         msg (if ?nested
                               (format "`%s` unconditionally returns a string." ?nested)
                               "Use the literal directly.")]
                     (->diagnostic ctx rule form {:replace-form new-form
                                                  :message msg})))))})
