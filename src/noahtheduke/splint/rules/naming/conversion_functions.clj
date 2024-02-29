; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.conversion-functions
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn to?? [sym]
  (and (symbol? sym)
    (str/includes? (name sym) "-to-")))

(defrule naming/conversion-functions
  "Use `->` instead of `to` in the names of conversion functions.

  Will only warn when there is no `-` before the `-to-`.

  Examples:

  ; bad
  (defn f-to-c ...)

  ; good
  (defn f->c ...)
  (defn expect-f-to-c ...)
  "
  {:pattern '((? _ defn??) (? f-name to??) ?*_)
   :message "Use `->` instead of `to` in the names of conversion functions."
   :on-match (fn [ctx rule form {:syms [?f-name]}]
               (let [[head tail] (str/split (name ?f-name) #"-to-")]
                 (when (and tail (not (str/includes? head "-")))
                   (let [form (list 'defn ?f-name '...)
                         new-form (list 'defn (symbol (str/replace (str ?f-name) "-to-" "->")) '...)]
                     (->diagnostic ctx rule form {:replace-form new-form
                                                  :form-meta (meta form)})))))})
