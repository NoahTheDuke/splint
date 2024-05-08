; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.prefer-method-values
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [string-interop-method?
                                             symbol-class?]]
   [noahtheduke.splint.config :refer [get-config]]))

(set! *warn-on-reflection* true)

(defn interop? [sym]
  (and (simple-symbol? sym)
    (str/starts-with? (name sym) ".")))

(defrule lint/prefer-method-values
  "Uniform qualified method values are a new syntax for calling into java code. They must resolve to a single static or instance method and to help with that, a new metadata syntax can be used: `^[]` aka `^{:param-tags []}`. Types are specified with classes, each corrosponding to an argument in the target method: `(^[long String] SomeClass/.someMethod 1 \"Hello world!\")`. It compiles to a direct call without any reflection, guaranteeing optimal performance.

  If it doesn't resolve to a single method, then the Clojure compiler throws a syntax error (IllegalArgumentException). Such ahead-of-time compilation checking is a powerful and helpful tool in writing correct and performant code. Given that, it is preferable to exclusively use method values.

  Examples:

  ; avoid
  (.toUpperCase \"noah\")
  (. \"noah\" toUpperCase)

  ; prefer
  (^[] String/toUpperCase \"noah\")
  "
  {:pattern '((? fn interop?) ?obj ?*args)
   :ext :clj
   :min-clojure-version {:major 1 :minor 12}
   :message "Prefer uniform Class/member syntax instead of traditional interop."
   :on-match (fn [ctx rule form {:syms [?fn ?obj ?args]}]
               (let [[?method ?args]
                     (if (= '. ?fn)
                       [(if (list? (first ?args))
                          (str "." (ffirst ?args))
                          (str "." (first ?args)))
                        (next ?args)]
                       [(str ?fn) ?args])]
                 (cond
                   ;; leave to style/prefer-clj-string
                   (and (string-interop-method? (symbol ?method))
                     (:enabled (get-config ctx 'style/prefer-clj-string)))
                   nil
                   ; Class object
                   (symbol-class? ?obj)
                   (let [new-call (symbol (str ?obj) (subs ?method 1))
                         new-form (list* new-call ?args)]
                     (->diagnostic ctx rule form {:replace-form new-form}))
                   :else
                   (let [new-call (symbol (str 'CLASS) ?method)
                         new-form (list* new-call ?obj ?args)]
                     (->diagnostic ctx rule form {:replace-form new-form})))))})
