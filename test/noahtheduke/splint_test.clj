; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint-test
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.config :refer [read-default-config]]
    [noahtheduke.spat.parser :refer [parse-string]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :refer [check-and-recur check-form prepare-rules]]))

(set! *warn-on-reflection* true)

(def config (read-default-config))

(defn make-rules []
  (prepare-rules config (or @global-rules {})))

(defn rules-for-form [form]
  ((make-rules) (simple-type form)))

(defn check-str
  [s]
  (let [ctx (atom {})
        form (parse-string s)]
    (check-form ctx (rules-for-form form) nil form)))

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
    (check-and-recur ctx (make-rules) "filename" nil form)
    (:diagnostics @ctx)))
