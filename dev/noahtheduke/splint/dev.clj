; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.dev
  (:require
    [clojure.java.io :as io]
    [nextjournal.beholder :as beholder]
    [noahtheduke.splint.config :as config]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint]
    [taoensso.tufte :as tufte]))

(set! *warn-on-reflection* true)

(doseq [dev-rule (file-seq (io/file "dev" "noahtheduke" "splint" "rules" "dev"))
        :when (.isFile ^java.io.File dev-rule)]
  (load-file (str dev-rule)))

(defn build-default-config []
  (let [dev-rules (->> (keys (:rules @global-rules))
                       (filter #(.equals "dev" (namespace %)))
                       (map (fn [r] (clojure.lang.MapEntry. r {:enabled true})))
                       (into {}))
        default-rules (config/read-default-config)]
    (merge dev-rules default-rules)))

(def dev-config (atom (build-default-config)))

(def watcher
  (beholder/watch
    (fn [action]
      (when (#{:create :modify} (:type action))
        (reset! dev-config (build-default-config))))
    "resources"))

(tufte/add-basic-println-handler! {})

(comment
  (beholder/stop watcher))
