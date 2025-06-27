; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.dev
  (:require
   [nextjournal.beholder :as beholder]
   [noahtheduke.splint]
   [noahtheduke.splint.config :as config]
   [noahtheduke.splint.rules :refer [global-rules]]))

(set! *warn-on-reflection* true)

(defn build-default-config []
  (load-file "dev/noahtheduke/splint/rules/dev/throws_on_match.clj")
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

(comment
  (beholder/stop watcher))
