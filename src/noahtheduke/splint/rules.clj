; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules
  "All rules-related functionality.

  Rules don't actually use defrecord, but act like they do with the following definition:

  (defrecord Rule
    [name genre full-name docstring init-type pattern-raw replace-raw message min-clojure-version ext pattern patterns on-match])"
  (:require
   [clojure.spec.alpha :as s]
   [noahtheduke.splint.pattern :as p]
   [noahtheduke.splint.utils :refer [simple-type]]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.replace :refer [postwalk-splicing-replace]]))

(set! *warn-on-reflection* true)

(defonce
  ^{:doc "All registered rules, grouped by :init-type and full-name"}
  global-rules
  (atom {:rules {} :genres #{}}))

(defn replace->diagnostic [replace-form]
  (fn [ctx rule form binds]
    (let [replaced-form (postwalk-splicing-replace binds replace-form)]
      (->diagnostic ctx rule form {:replace-form replaced-form}))))

(defmacro defrule
  "Define a new rule.

  Must include:

  * EITHER `:pattern` or `:patterns`,
  * EITHER `:replace` or `:on-match`"
  {:arglists '([rule-name docs {:keys [pattern patterns replace on-match
                                       message init-type min-clojure-version
                                       ext autocorrect] :as opts}])}
  [rule-name docs opts]
  ;; Babashka-compatible instrumentation, cribbed from clojure.spec.alpha/macro-expand-check
  (let [invocation (list rule-name docs opts)]
    (when (s/invalid? (s/conform ::defrule invocation))
      (throw (ex-info (format "Call to %s did not conform to spec" `defrule)
                      (assoc (s/explain-data* ::defrule [] [::defrule] [] invocation)
                             ::s/args invocation)))))
  (let [{:keys [pattern patterns replace on-match message init-type
                min-clojure-version ext autocorrect]} opts]
    (assert (not (and pattern patterns))
      "defrule cannot define both :pattern and :patterns")
    (when patterns
      (assert (apply = (map simple-type patterns))
        "All :patterns should have the same `simple-type`"))
    (assert (not (and replace on-match))
      "defrule cannot define both :replace and :on-match")
    (let [full-name rule-name
          rule-name (name full-name)
          genre (namespace full-name)
          init-type (or init-type
                      (if pattern
                        (simple-type pattern)
                        (simple-type (first patterns))))]
      `(let [rule# {:name ~rule-name
                    :genre ~genre
                    :full-name '~full-name
                    :docstring ~docs
                    :init-type ~init-type
                    :pattern-raw ~(or pattern patterns)
                    :replace-raw ~replace
                    :message ~message
                    :min-clojure-version ~min-clojure-version
                    :ext ~(cond
                            (set? ext) ext
                            (keyword? ext) #{ext})
                    :pattern (when ~(some? pattern) (p/pattern ~pattern))
                    :patterns (when ~(some? patterns)
                                ~(mapv #(list `p/pattern %) patterns))
                    :on-match (or ~on-match (replace->diagnostic ~replace))
                    :autocorrect ~autocorrect}]
         (swap! global-rules #(-> %
                                (assoc-in [:rules '~full-name] rule#)
                                (update :genres conj '~(symbol genre))))
         (def ~(symbol rule-name) ~docs rule#)))))

(s/def ::rule-name qualified-symbol?)
(s/def ::docs string?)
(s/def ::pattern any?)
(s/def ::patterns (s/and vector? (s/+ any?)))
(s/def ::replace any?)
(s/def ::on-match (s/and seq? #(.equals "fn" (name (first %)))))
(s/def ::message string?)
(s/def ::init-type keyword?)
(s/def ::major int?)
(s/def ::minor int?)
(s/def ::incremental int?)
(s/def ::min-clojure-version (s/keys :opt-un [::major ::minor ::incremental]))
(s/def ::ext (s/or :single keyword? :multiple (s/coll-of keyword? :kind vector?)))
(s/def ::autocorrect boolean?)
(s/def ::opts (s/keys :req-un [(or ::pattern ::patterns)
                               (or ::replace ::on-match)]
                :opt-un [::message ::init-type ::min-clojure-version ::ext ::autocorrect]))

(s/def ::defrule
  (s/cat :rule-name ::rule-name
    :docs ::docs
    :opts ::opts))
