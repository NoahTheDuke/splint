; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.metrics.fn-length
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [defn??]]))

(set! *warn-on-reflection* true)

(defn check-size
  [ctx config rule form form-str]
  (when-let [m (meta form)]
    (let [len (- (:end-line m 0) (:line m 0))
          config-length (or (:length config) 10)]
      (when (< config-length len)
        (let [message (format "%s shouldn't be longer than %s lines."
                        form-str
                        config-length)]
          (->diagnostic ctx rule form {:message message}))))))

(defn defn-size
  [ctx config rule form]
  (check-size ctx config rule form "defn forms"))

(defn body-size
  [ctx config rule defn-form]
  (keep
    (fn [fn-body] (check-size ctx config rule fn-body "Function bodies"))
    (:arities defn-form)))

(defrule metrics/fn-length
  "Avoid `defn`-defined functions longer than some number (10) of lines of code. Longer functions are harder to read and should be split into smaller-purpose functions that are composed.

  The total length is configurable, and the size can be configured (`:body` or `:defn` styles) to be measured from the entire `defn` form or the vector+body.

  @examples

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
  {:pattern '((? _ defn??) ?*args)
   :on-match (fn [ctx rule form bindings]
               (when-let [defn-form (:splint/defn-form (meta form))]
                 (let [config (:config rule)]
                   (condp = (:chosen-style config)
                     :defn (defn-size ctx config rule form)
                     :body (body-size ctx config rule defn-form)))))})
