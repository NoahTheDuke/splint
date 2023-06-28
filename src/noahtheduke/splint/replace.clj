; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.replace
  (:require
    [noahtheduke.splint.clojure-ext.core :refer [postwalk*]]))

(set! *warn-on-reflection* true)

(defn- splicing-replace [sexp]
  (let [[front-sexp rest-sexp] (split-with #(not= '&&. %) sexp)]
    (concat front-sexp (second rest-sexp) (drop 2 rest-sexp))))

(defn postwalk-splicing-replace [binds replace-form]
  (postwalk*
    (fn [item]
      (cond
        (seq? item) (splicing-replace item)
        (contains? binds item) (binds item)
        :else
        item))
    replace-form))
