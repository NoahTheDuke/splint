; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.metrics.parameter-count
  (:require
    [noahtheduke.splint.config :refer [get-config]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn count-positional-params [arglist]
  (let [arglist-count (count arglist)]
    (if (and (< 2 arglist-count) (= '& (nth arglist (- arglist-count 2))))
      (- arglist-count 2)
      arglist-count)))

(defn build-diagnostic [rule filename config-count chosen-style arglist]
  (let [param-count (condp = chosen-style
                      :positional (count-positional-params arglist)
                      :include-rest (count arglist))]
    (when (and param-count (< config-count param-count))
      (let [rest-msg (if (= :include-rest chosen-style)
                       " (including & rest parameters)"
                       "")
            message (format "Avoid parameter lists with more than %s parameters%s."
                            config-count
                            rest-msg)]
        (->diagnostic rule arglist {:message message
                                    :filename filename})))))

(defrule metrics/parameter-count
  "Avoid parameter lists with more than 4 positional parameters.

  The number of parameters can be configured with `:count`. The default style `:positional` excludes `& args` rest parameters, and the style `:include-rest` includes them.

  Functions with multiple arities will have each arity checked.

  Examples:

  ;; :positional style (default)
  # bad
  (defn example [a b c d e] ...)
  (defn example ([a b c d e] ...) ([a b c d e f g] ...))
  (defn example [a b c d e & args] ...)

  # good
  (defn example [a b c d] ...)
  (defn example ([a b c] ...) ([a b c e] ...))
  (defn example [a b c d & args] ...)

  ;; :include-rest style
  # bad
  (defn example [a b c d & args] ...)

  # good
  (defn example [a b c & args] ...)
  "
  {:pattern '(%defn-fn?? &&. ?args)
   :on-match (fn [ctx rule form bindings]
               (when-let [defn-form (:spat/defn-form (meta form))]
                 (let [config (get-config ctx rule)
                       config-count (:count config)
                       chosen-style (:chosen-style config)
                       filename (:filename (meta form))]
                   (->> (:arglists defn-form)
                        (keep #(build-diagnostic rule filename config-count chosen-style %))))))})
