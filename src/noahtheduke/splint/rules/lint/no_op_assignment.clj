; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.no-op-assignment
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/no-op-assignment
  "If the bind is a symbol and the expr is the same symbol, just use the expr directly. (Otherwise, indicates a potential bug.)

  @examples

  ; avoid
  (let [foo foo]
    ...)
  "
  {:pattern '(let [?+bindings] ?*_)
   :on-match (fn [ctx rule form {:syms [?bindings]}]
               (when (even? (count ?bindings))
                 (for [[bind expr] (partition 2 ?bindings)
                       :when (and (symbol? bind)
                               (= bind expr)
                               (not (:splint/reader-cond (meta expr))))
                       :let [new-form (with-meta (list bind expr)
                                        (meta bind))]]
                   (->diagnostic ctx rule new-form {:message "Avoid no-op assignment."}))))})
