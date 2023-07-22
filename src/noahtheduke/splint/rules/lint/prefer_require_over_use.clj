; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.prefer-require-over-use
  (:require
    [noahtheduke.splint.config :refer [get-config]]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.rules :refer [defrule]]))

(defn find-use-libs [?libspecs]
  (->> ?libspecs
       (filter #(and (sequential? ?libspecs) (= :use (first %))))
       (seq)))

(defn make-message [ctx rule]
  (condp = (:chosen-style (get-config ctx rule))
    :as "Use (:require [some.lib :as l]) over (:use some.lib)"
    :refer "Use (:require [some.lib :refer [...]]) over (:use some.lib)"
    :all "Use (:require [some.lib :refer :all]) over (:use some.lib)"
    nil))

(defrule lint/prefer-require-over-use
  "In the `ns` form prefer `:require :as` over `:require :refer` over `:require :refer :all`. Prefer `:require` over `:use`; the latter form should be considered deprecated for new code.

  Examples:

  # bad
  (ns examples.ns
    (:use clojure.zip))

  # good
  (ns examples.ns
    (:require [clojure.zip :as zip]))
  (ns examples.ns
    (:require [clojure.zip :refer [lefts rights]]))
  (ns examples.ns
    (:require [clojure.zip :refer :all]))
  "
  {:pattern2 '(ns ?ns ?*libspecs)
   :on-match (fn [ctx rule form {:syms [?libspecs]}]
               (when-let [use-libs (find-use-libs ?libspecs)]
                 (when-let [message (make-message ctx rule)]
                   (for [_use-body use-libs]
                     (->diagnostic ctx rule form {:message message})))))})
