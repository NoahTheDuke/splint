; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-var-dispatch
  (:require
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [var??]]))

(set! *warn-on-reflection* true)

(defn not-var?
  [obj]
  (not (or
         (string? obj)
         (map? obj)
         (and (list? obj)
           (var?? (first obj))))))

(defrule style/prefer-var-dispatch
  "During repl-driven development, one might wish to change the dispatch function of a multimethod. However, once set, the dispatch function cannot be changed without first unmapping the entire multimethod, which requires reloading all methods again.

  To avoid that, define a dispatch function and use it as a var for the dispatch function of the multimethod.

  @examples

  ; avoid
  (defmulti example :type)

  ; prefer
  (def example-dispatch :type)
  (defmulti example #'example-dispatch)
  "
  {:pattern '(defmulti ?name (?? ?docstring string?) (?? ?attr map?)
               (? ?dispatch not-var?) (?* ?options))
   :message "Use a var for the dispatch function to improve repl-drive development."})
