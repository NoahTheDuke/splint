; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.existing-constant
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [clojure.string :as str]
   [noahtheduke.splint.utils :refer [support-clojure-version?]]))

(set! *warn-on-reflection* true)

(defn fp? [n]
  (or (float? n)
    (double? n)))

(def constants
  [["3.14" 'java.lang.Math/PI 'clojure.math/PI]
   ["2.718" 'java.lang.Math/E 'clojure.math/E]])

(defn check-size [?constant use-clojure-math?]
  (let [s (str ?constant)]
    (first
      (for [[start java const] constants
            :when (str/starts-with? s start)]
        (if use-clojure-math?
          const
          java)))))

(defrule lint/existing-constant
  "Java has `PI` and `E` constants built-in, and `clojure.math` exposes them directly. Better to use them instead of poorly approximating them with vars.

  @examples

  ; avoid
  (def pi 3.14)
  (def e 2.718)

  ; prefer (Clojure 1.11+)
  clojure.math/PI
  clojure.math/E

  ; prefer (Clojure 1.10)
  java.lang.Math/PI
  java.lang.Math/E
  "
  {:pattern '(def ?name (? constant fp?))
   :ext :clj
   :on-match (fn [ctx rule form {:syms [?name ?constant]}]
               (let [use-clojure-math? (support-clojure-version?
                                         {:major 1 :minor 11}
                                         (:clojure-version (:config ctx)))]
                 (when-let [existing (check-size ?constant use-clojure-math?)]
                   (let [message (format "Use %s directly" (str existing))]
                     (->diagnostic ctx rule form {:message message
                                                  :replace-form existing})))))})
