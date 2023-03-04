; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser
  (:require
    [edamame.core :as e]))

(set! *warn-on-reflection* true)

(def clj-defaults
  {; :all true
   :deref true
   :fn true
   :quote true
   :read-eval true
   :regex true
   :syntax-quote true
   :var true
   :row-key :line
   :col-key :column
   :end-location true
   :location? seq?
   :features #{:cljs}
   :read-cond :preserve
   :auto-resolve (fn [k] (if (= :current k) 'splint (name k)))
   :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "splint" (name r))) v)))})

(defn parse-string [s] (e/parse-string s clj-defaults))
(defn parse-string-all [s] (e/parse-string-all s clj-defaults))
