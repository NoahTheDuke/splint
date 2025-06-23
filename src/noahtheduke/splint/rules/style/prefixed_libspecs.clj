; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.prefixed-libspecs
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.utils :refer [drop-quote]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn find-nested-libspecs [?libspecs]
  (->> ?libspecs
    (filter #(let [x (drop-quote %)]
               (and (sequential? x)
                 (vector? (second x)))))
    (seq)))

;; TODO: figure out how to suggest alternatives
(defn build
  ([reqs] (build nil reqs))
  ([prefix reqs]
    (let [[head & tail] reqs]
      (if (and (seq tail) (vector? (first tail)))
        (mapv #(build (cons head prefix) %) tail)
        (do (prn :head head :tail tail)
          (vec (list* (symbol (str/join "." (reverse (cons head prefix))))
                    tail)))))))

(comment
  (build '[clojure.string :as str])
  (build '[clojure [string :as str] [set :as set]]))

(defrule style/prefixed-libspecs
  "`require` supports prefixed libspecs, shared \"parent\" namespaces with subsections in separate vectors. This allows for 'DRY' libspecs but harms readability and discoverability while not actually providing a great reduction in characters.

  @examples

  ; avoid
  (ns foo.bar
    (:require [clojure
               [string :as str]
               [set :as set]]))

  (require '[clojure
             [string :as str]
             [set :as set]])

  ; prefer
  (ns foo.bar
    (:require [clojure.string :as str]
              [clojure.set :as set]))

  (require '[clojure.string :as str]
    '[clojure.set :as set])
  "
  {:patterns ['(:require ?*libspecs)
              '(require ?*reqs)]
   :on-match (fn [ctx rule form {:syms [?libspecs ?reqs]}]
               ;; (:require ...)
               (if-let [libspecs (when (= 'ns (first (:parent-form ctx)))
                                   (find-nested-libspecs ?libspecs))]
                 (for [original libspecs]
                   (->diagnostic ctx rule original
                     {:message "Don't use prefix libspecs in require calls"}))
                 ;; (require ...)
                 (when-let [libspecs (find-nested-libspecs ?reqs)]
                   (for [original libspecs]
                     (->diagnostic ctx rule original
                       {:message "Don't use prefix libspecs in require calls"})))))})
