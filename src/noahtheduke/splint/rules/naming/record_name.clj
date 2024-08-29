; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.record-name
  (:require
   [camel-snake-kebab.core :as csk]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn bad-name? [sexp]
  (let [record-name (str sexp)]
    (not= record-name (csk/->PascalCase record-name))))

(defrule naming/record-name
  "Records should use PascalCase. (Replacement is generated with [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).)

  @examples

  ; avoid
  (defrecord foo [a b c])
  (defrecord foo-bar [a b c])
  (defrecord Foo-bar [a b c])

  ; prefer
  (defrecord Foo [a b c])
  (defrecord FooBar [a b c])
  "
  {:pattern '(defrecord (? record-name bad-name?) ?*args)
   :message "Records should use PascalCase."
   :on-match (fn [ctx rule form {:syms [?record-name ?args]}]
               (let [new-record-name (symbol (csk/->PascalCase ?record-name))
                     new-form (list* 'defrecord new-record-name ?args)]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
