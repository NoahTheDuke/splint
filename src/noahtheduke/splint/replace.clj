; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace
  (:require
    [noahtheduke.splint.pattern :as-alias p]
    [noahtheduke.splint.clojure-ext.core :refer [postwalk*]]))

(set! *warn-on-reflection* true)

(defn- splicing-replace [item]
  (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) item)
        new-item (concat front-sexp (second rest-sexp) (drop 2 rest-sexp))
        new-item (reduce
                   (fn [acc cur]
                     (if (::p/rest (meta cur))
                       (into acc cur)
                       (conj acc cur)))
                   []
                   new-item)]
    (if (vector? item)
      new-item
      (seq new-item))))

(defn postwalk-splicing-replace [binds replace-form]
  (postwalk*
    (fn [item]
      (cond
        (seq? item) (splicing-replace item)
        (contains? binds item) (binds item)
        :else
        item))
    replace-form))
