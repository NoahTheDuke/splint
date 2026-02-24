; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser.defmulti
  (:require
   [noahtheduke.splint.clojure-ext.core :refer [get-arg update-vals*]]
   [noahtheduke.splint.utils :refer [drop-quote]]))

(set! *warn-on-reflection* true)

(defn parse-defmulti
  [form]
  (let [opts (next form)
        [multi-name opts] (get-arg opts symbol?)
        [docstring opts] (get-arg opts string?)
        [attr-map opts] (get-arg opts map?)
        attr-map (when attr-map
                   (-> attr-map
                     (drop-quote)
                     (update-vals* drop-quote)))
        [dispatch-fn opts] (if (seq opts)
                             (get-arg opts any?)
                             [:splint/missing-dispatch-fn])
        options (when (even? (count opts))
                  (apply hash-map opts))]
    (when multi-name
      (cond-> {:splint/name multi-name
               :splint/raw-opts opts
               :dispatch-fn dispatch-fn}
        docstring (assoc :doc docstring)
        attr-map (conj attr-map)
        options (assoc :options options)))))

(comment
  (parse-defmulti
    '(defmulti example "pants" {:foo true} :type :hierarchy local-hierarcy)))
