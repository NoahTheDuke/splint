; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.no-target-for-method
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn instance-interop? [sexp]
  (and (symbol? sexp)
    (str/starts-with? (name sexp) ".")))

(defn in-nested-ctx? [ctx]
  (when-let [parent-form (:parent-form ctx)]
    (and (seq? parent-form)
      (#{'doto 'case '-> '->> 'cond-> 'cond->> 'some-> 'some->>} (first parent-form)))))

(defrule lint/no-target-for-method
  "Instance methods require a target instance. If there's none or it's nil, highly likely there's a bug.

  This rule ignores when a find is nested in a `doto` or similar form: `(doto (new java.util.HashMap) (.put \"a\" 1) (.put \"b\" 2))` will not raise a diagnostic.

  @examples

  ; avoid
  (.length)
  (.length nil)
  (String/.length)
  (String/.length nil)

  ; prefer
  (.length foo)
  (String/.length foo)
  "
  {:pattern '((? ?fn instance-interop?) (?? _ nil?))
   :on-match (fn [ctx rule form {:syms [?fn]}]
               (let [parent (:parent-form ctx)]
                 (when-not (in-nested-ctx? ctx)
                   (->diagnostic ctx rule form
                     {:message "Instance methods require a target instance."}))))})
