; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefer-for-with-literals
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.clojure-ext.core :refer [postwalk*]]))

(set! *warn-on-reflection* true)

(defn builder [form]
  (and (symbol? form)
    (#{"vector" "hash-map" "array-map" "hash-set"} (name form))))

(defn replace-fn-arg [arg new-arg builder-args]
  (postwalk*
    (fn [item]
      (if (= arg item) new-arg item))
    builder-args))

(defn builder->literal [builder-fn args]
  (case (name builder-fn)
    ("array-map" "hash-map") (apply sorted-map args)
    "hash-set" (apply hash-set args)
    "vector" (apply vector args)))

(defrule style/prefer-for-with-literals
  "The core builder functions are helpful when creating an object from an opaque sequence, but are much less readable when used in maps to get around issues with anonymous function syntax peculiarities.

  Examples:

  ; avoid
  (map #(hash-map :a 1 :b %) (range 10))

  ; prefer
  (for [item (range 10)] {:a 1 :b item})
  "
  {:pattern '(map ((? _ fn??) (?? _ symbol?) [?arg]
                              ((? builder-fn builder) (?* builder-args)))
               (? coll))
   :message "Prefer `for` when creating a seq of data literals."
   :on-match (fn [ctx rule form {:syms [?arg ?builder-fn ?builder-args ?coll] :as wtf}]
               (let [builder-args (replace-fn-arg ?arg 'item ?builder-args)
                     new-form (list 'for ['item ?coll]
                                (builder->literal ?builder-fn builder-args))]
                 (->diagnostic ctx rule form {:replace-form new-form})))})
