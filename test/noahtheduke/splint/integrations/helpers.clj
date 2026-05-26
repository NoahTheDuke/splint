; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.integrations.helpers 
  (:require
   [noahtheduke.splint.clojure-ext.core :refer [update-vals*]]
   [noahtheduke.splint.config :refer [merge-config read-default-config]]))

(set! *warn-on-reflection* true)

(defn usefully-enabled-config
  ([] (usefully-enabled-config nil))
  ([overrides]
   (-> (read-default-config)
       (update-vals* #(assoc % :enabled true))
       (assoc-in ['style/set-literal-as-fn :enabled] false)
       (merge-config nil)
       (merge {:silent true
               :parallel false
               :clojure-version {:major 1 :minor 11}}
              overrides))))
