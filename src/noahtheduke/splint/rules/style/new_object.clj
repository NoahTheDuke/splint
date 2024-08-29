; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.new-object
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.utils :refer [support-clojure-version?]]))

(set! *warn-on-reflection* true)

(defn end-with-period? [sexp]
  (and (symbol? sexp)
    (str/ends-with? (str sexp) ".")))

(defrule style/new-object
  "`new` special form is discouraged for dot usage.

  @note
  The style `:method-value` requires Clojure version 1.12+.

  @examples

  ; avoid
  (new java.util.ArrayList 100)

  ; prefer (chosen style :dot (default))
  (java.util.ArrayList. 100)

  ; avoid (chosen style :method-value)
  (java.util.ArrayList. 100)

  ; prefer (chosen style :method-value)
  (java.util.ArrayList/new 100)
  "
  {:patterns ['(new ?class ?*args)
              '((? class end-with-period?) ?*args)]
   :autocorrect true
   :on-match (fn [ctx rule form {:syms [?class ?args]}]
               (let [chosen-style (:chosen-style (:config rule))
                     dot-call? (str/ends-with? (str ?class) ".")]
                 (when-not (and (= :dot chosen-style) dot-call?)
                   (let [use-method? (and (support-clojure-version?
                                            {:major 1 :minor 12}
                                            (:clojure-version (:config ctx)))
                                       (= :method-value chosen-style))
                         ?class (str ?class)
                         ?class (if dot-call?
                                  (subs ?class 0 (unchecked-dec (.length ?class)))
                                  ?class)
                         new-class (symbol (if use-method?
                                             (str ?class "/new")
                                             (str ?class ".")))
                         new-form (list* new-class ?args)
                         message (if use-method?
                                   "Foo/new is preferred."
                                   "Foo. is preferred.")]
                     (->diagnostic ctx rule form {:replace-form new-form
                                                  :message message})))))})
