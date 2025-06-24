; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.existing-constant
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn fp? [n]
  (or (float? n)
    (double? n)))

(def constants
  [['clojure.math/PI "3.14"]
   ['clojure.math/E "2.718"]])

(defn check-size [n]
  (let [s (str n)]
    (first
      (for [[const start] constants
            :when (str/starts-with? s start)]
        const))))

(defrule lint/existing-constant
  "Java has `PI` and `E` constants built-in, and `clojure.math` exposes them directly. Better to use them instead of poorly approximating them with vars.

  @examples

  ; avoid
  (def pi 3.14)
  (def e 2.718)

  ; prefer
  clojure.math/PI
  clojure.math/E
  "
  {:pattern '(def ?name (? constant fp?))
   :min-clojure-version {:major 1 :minor 11}
   :on-match (fn [ctx rule form {:syms [?name ?constant]}]
               (when-let [existing (check-size ?constant)]
                 (let [message (format "Use %s directly" (str existing))]
                   (->diagnostic ctx rule form {:message message
                                                :replace-form existing}))))})
