; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace
  (:require
   [noahtheduke.splint.pattern :as-alias p]
   [noahtheduke.splint.clojure-ext.core :refer [postwalk* ->list]]))

(set! *warn-on-reflection* true)

(defn- splicing-replace [item]
  (let [new-item (reduce
                   (fn [acc cur]
                     (if (::p/rest (meta cur))
                       (into acc cur)
                       (conj acc cur)))
                   []
                   item)]
    (if (vector? item)
      new-item
      (->list new-item))))

(defn postwalk-splicing-replace [binds replace-form]
  (postwalk*
    (fn [item]
      (cond
        (seq? item) (splicing-replace item)
        (contains? binds item) (binds item)
        :else
        item))
    replace-form))
