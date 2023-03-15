; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint-test
  (:require
    noahtheduke.splint
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.config :refer [read-default-config]]
    [noahtheduke.spat.parser :refer [parse-string]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :refer [check-and-recur check-form]]))

(set! *warn-on-reflection* true)

(def config (read-default-config))

(defn rules-for-form [form]
  (@global-rules (simple-type form)))

(defn check-str
  [s]
  (let [ctx (atom {})
        form (parse-string s)]
    (check-form ctx config (rules-for-form form) form)))

(defn check-alt
  [s]
  (:alt (first (check-str s))))

(defn check-message
  [s]
  (:message (first (check-str s))))

(defn check-all
  [s]
  (let [ctx (atom {})
        form (parse-string s)]
    (check-and-recur ctx config @global-rules "filename" form)
    (:diagnostics @ctx)))
