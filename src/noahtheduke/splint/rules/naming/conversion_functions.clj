; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.conversion-functions
  (:require
    [noahtheduke.splint.rules :refer [defrule ->violation]]
    [clojure.string :as str]))

(defrule naming/conversion-functions
  "Use `->` instead of `to` in the names of conversion functions.

  Examples:

  # bad
  (defn f-to-c ...)

  # good
  (defn f->c ...)
  "
  {:pattern '(defn ?f-name &&. ?args)
   :message "Use `->` instead of `to` in the names of conversion functions."
   :on-match (fn [rule form {:syms [?f-name ?args]}]
               (when (str/includes? (str ?f-name) "-to-")
                 (let [new-form (list* 'defn (symbol (str/replace (str ?f-name) "-to-" "->")) ?args)]
                   (->violation rule form {:replace-form new-form}))))})
