; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.into-literal
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn set-or-vec? [form]
  (and (or (set? form)
         (vector? form))
    (empty? form)))

(defn check-parent [ctx]
  (when-let [parent-form (:parent-form ctx)]
    (and (list? parent-form)
         (symbol? (first parent-form))
         (let [s (name (first parent-form))]
           (or (str/ends-with? s "->")
               (str/ends-with? s "->>"))))))

(defrule lint/into-literal
  "`vec` and `set` are succinct and meaningful.

  @examples

  ; avoid
  (into [] coll)

  ; prefer
  (vec coll)

  ; avoid
  (into #{} coll)

  ; prefer
  (set coll)
  "
  {:pattern '(into (? literal set-or-vec?) ?coll)
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?literal ?coll]}]
               (when-not (check-parent ctx)
                 (let [replace-form (list (if (set? ?literal) 'set 'vec) ?coll)
                       message (format "Use `%s` instead of recreating it."
                                       (if (set? ?literal) "set" "vec"))]
                   (->diagnostic ctx rule form {:replace-form replace-form
                                                :message message}))))})
