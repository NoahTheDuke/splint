; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.dev
  (:require
    [clojure.java.io :as io]
    [nextjournal.beholder :as beholder]
    [noahtheduke.splint.config :as config]))

(doseq [dev-rule (file-seq (io/file "dev" "noahtheduke" "splint" "rules" "dev"))
        :when (.isFile dev-rule)]
  (load-file (str dev-rule)))

(def default-config (atom (config/read-default-config)))

(def watcher
  (beholder/watch
    (fn [action]
      (when (#{:create :modify} (:type action))
        (reset! default-config (config/read-default-config))))
    "resources"))

(comment
  (beholder/stop watcher))
