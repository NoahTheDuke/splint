; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.prefer-method-values
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn interop? [sym]
  (and (simple-symbol? sym)
    (str/starts-with? (name sym) ".")))

(defrule lint/prefer-method-values
  "Uniform qualified method values are a new syntax for calling into java code. They must resolve to a single static or instance method and to help with that, a new metadata syntax can be used: `^[]` aka `^{:param-tags []}`. Types are specified with classes, each corrosponding to an argument in the target method: `(^[long String] SomeClass/someMethod 1 \"Hello world!\")`. It compiles to a direct call without any reflection, guaranteeing optimal performance.

  If it doesn't resolve to a single method, then the Clojure compiler throws a syntax error (IllegalArgumentException). Such ahead-of-time compilation checking is a powerful and helpful tool in writing correct and performant code. Given that, it is preferable to exclusively use method values.

  Examples:

  ; bad
  (.toUpperCase \"noah\")
  (. \"noah\" toUpperCase)

  ; good
  (^[] String/toUpperCase \"noah\")
  "
  {:pattern '((? _ interop?) (?* _))
   :ext :clj
   :min-clojure-version {:major 1 :minor 12}
   :on-match (fn [ctx rule form _binds]
               (let [msg "Prefer uniform Class/member syntax instead of traditional interop."]
                 (->diagnostic ctx rule form {:message msg})))})
