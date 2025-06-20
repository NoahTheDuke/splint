; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.redundant-nested-call
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/redundant-nested-call
  "Some clojure.core functions and macros take a variable number of args, so there's no need to nest calls. To check non-clojure.core functions, they can be added to the config under key `:fn-names`: `style/redundant-nested-call {:fn-names [foo]}`.

  > [!NOTE]
  > This can have performance implications in certain hot-loops.

  @examples

  ; avoid
  (+ 1 2 (+ 3 4))
  (comp :foo :bar (comp :qux :ply))

  ; prefer
  (+ 1 2 3 4)
  (comp :foo :bar :qux :ply)

  ; with `:fn-names [foo]`
  ; avoid
  (foo 1 2 (foo 3 4))

  ; prefer
  (foo 1 2 3 4)
  "
  {:pattern '(?fun ?+args (?fun ?+others))
   :on-match (fn [ctx {{:keys [fn-names]} :config :as rule} form {:syms [?fun ?args ?others]}]
               (when (and (symbol? ?fun)
                       (contains? fn-names (symbol (name ?fun))))
                 (let [new-form (list* ?fun (concat ?args ?others))]
                   (->diagnostic ctx rule form {:message (format "Redundant nested call: `%s`." ?fun)
                                                :replace-form new-form}))))})
