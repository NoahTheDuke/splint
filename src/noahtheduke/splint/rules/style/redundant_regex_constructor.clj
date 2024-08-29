; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.redundant-regex-constructor
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.pattern :refer [pattern]]))

(set! *warn-on-reflection* true)

(def regex-pat? (pattern '(splint/re-pattern ?reg-str)))

(defrule style/redundant-regex-constructor
  "Clojure regex literals (#\"\") are passed to `java.util.regex.Pattern/compile` at read time. `re-pattern` checks if the given arg is a Pattern, making it a no-op when given a regex literal.

  @examples

  ; avoid
  (re-pattern #\".*\")

  ; prefer
  #\".*\"
  "
  {:pattern '(re-pattern ?pat)
   :message "Rely on regex literal directly."
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?pat]}]
               (if (string? ?pat)
                 (let [new-form (list 'splint/re-pattern (str (re-pattern ?pat)))]
                   (->diagnostic ctx rule form {:replace-form new-form}))
                 (when-let [{:syms [?reg-str]} (regex-pat? ?pat)]
                   (let [new-form (list 'splint/re-pattern ?reg-str)]
                     (->diagnostic ctx rule form {:replace-form new-form})))))})
