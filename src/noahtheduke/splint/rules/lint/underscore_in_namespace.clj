; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.underscore-in-namespace
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/underscore-in-namespace
  "Due to munging rules, underscores in namespaces can confuse tools and
  libraries which expect that underscores in class names should be dashes in
  Clojure.

  Examples:

  # bad
  (ns foo_bar.baz_qux)

  # good
  (ns foo-bar.baz-qux)"
  {:pattern '(ns ?ns-sym ?*_)
   :message "Avoid underscores in namespaces."
   :on-match (fn [ctx rule form {:syms [?ns-sym]}]
               (when (str/includes? (str ?ns-sym) "_")
                 (let [new-namespace (symbol (str/replace (str ?ns-sym) "_" "-"))]
                   (->diagnostic ctx rule form {:replace-form new-namespace
                                                :form-meta (meta ?ns-sym)}))))})
