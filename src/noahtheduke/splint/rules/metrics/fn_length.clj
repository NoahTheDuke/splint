; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.metrics.fn-length
  (:require
    [noahtheduke.splint.config :refer [get-config]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn check-size
  [config rule form form-str]
  (when-let [m (meta form)]
    (let [len (- (:end-row m 0) (:line m 0))
          config-length (or (:length config) 10)]
      (when (< config-length len)
        (let [message (format "%s shouldn't be longer than %s lines."
                              form-str
                              config-length)]
          (->diagnostic rule form {:message message}))))))

(defn defn-size
  [config rule form]
  (check-size config rule form "defn forms"))

(defn body-size
  [config rule defn-form]
  (keep
    (fn [fn-body] (check-size config rule fn-body "Function bodies"))
    (:arities defn-form)))

(defrule metrics/fn-length
  "Avoid `defn`-defined functions longer than some number (10) of lines of code. Longer functions are harder to read and should be split into smaller-purpose functions that are composed.

  The total length is configurable, and the size can be configured (`:body` or `:defn` styles) to be measured from the entire `defn` form or the vector+body.

  Examples:

  ;; :body style (default)
  (defn foo
    [arg] ;; <- starts here
    0
    1
    ...
    9
    10) ;; <- ends here

  (defn foo
    ([] (foo 100)) ;; <- starts and ends here
    ([arg] ;; <- starts here
     0
     1
     ...
     9
     10)) ;; <- ends here

  ;; :defn style
  (defn foo ;; <- starts here
    [arg]
    0
    1
    ...
    9
    10) ;; <- ends here

  (defn foo ;; <- starts here
    ([] (foo 100))
    ([arg]
     0
     1
     ...
     9
     10)
  ) ;; <- ends here
  "
  {:pattern '(%defn?? &&. ?_args)
   :on-match (fn [ctx rule form bindings]
               (when-let [defn-form (:spat/defn-form (meta form))]
                 (let [config (get-config ctx rule)]
                   (condp = (:chosen-style config)
                     :defn (defn-size config rule form)
                     :body (body-size config rule defn-form)
                     ; else
                     nil))))})