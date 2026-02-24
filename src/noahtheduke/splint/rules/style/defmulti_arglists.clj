; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.defmulti-arglists
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule style/defmulti-arglists
  "Due to the way multimethods can use any IFn for dispatch, it is rare for a multimethod to have the proper `:arglists` metadata. It is a small thing but adding that can improve instrospection in repl-driven development (`clojure.repl/doc`) as well as external tooling (`clj-kondo`, `clojure-lsp`).

  @examples

  ; avoid
  (defmulti example #'dispatch-fn)

  ; prefer
  (defmulti example {:arglists '([obj])} #'dispatch-fn)
  "
  {:pattern '(defmulti ?name ?*options)
   :on-match (fn [ctx rule form {:syms [?name ?options]}]
               (when-let [defmulti-form (:splint/defmulti-form (meta form))]
                 (when-not (:arglists defmulti-form)
                   (->diagnostic ctx rule form {:message "Specify the :arglists to help documentation and tooling."}))))})
