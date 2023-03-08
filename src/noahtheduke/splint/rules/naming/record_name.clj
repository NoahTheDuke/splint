; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.record-name
  (:require
    [clojure.string :as str]
    [noahtheduke.splint.rules :refer [defrule ->violation]]))

(defn lower-case-name? [sexp]
  (let [record-name (str sexp)]
    (= (subs record-name 0 1)
       (str/lower-case (subs record-name 0 1)))))

(defrule naming/record-name
  "Records should use PascalCase.

  Examples:

  # bad
  (defrecord foo [a b c])

  # good
  (defrecord Foo [a b c])
  "
  {:pattern '(defrecord ?record-name &&. ?args)
   :message ""
   :on-match (fn [rule form {:syms [?record-name ?args]}]
               (when (lower-case-name? ?record-name)
                 (let [new-record-name (symbol
                                         (str (subs (str/upper-case (str ?record-name)) 0 1)
                                              (subs (str ?record-name) 1)))
                       new-form (list* 'defrecord new-record-name ?args)]
                   (->violation rule form {:replace-form new-form}))))})
