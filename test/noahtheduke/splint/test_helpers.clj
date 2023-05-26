; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    matcher-combinators.test
    [clojure.spec.alpha :as s]
    [expectations.clojure.test :refer [expect]]
    [noahtheduke.spat.pattern :refer [drop-quote]]
    [noahtheduke.splint.config :refer [deep-merge]]
    [noahtheduke.splint.dev :as dev]
    [noahtheduke.splint.runner :refer [run-impl]]))

(set! *warn-on-reflection* true)

(defn check-all
  ([path] (check-all path nil))
  ([path config] (check-all path config nil))
  ([path config options]
   (let [start-time (System/currentTimeMillis)
         config (conj {:dev true} (deep-merge @dev/default-config config))
         results (run-impl start-time options path config)]
     (seq (:diagnostics results)))))

(defmacro expect-match
  ([expected s] `(expect-match ~expected ~s nil))
  ([expected s config]
   `(let [diagnostics# (check-all ~s ~config nil)]
      (expect (~'match? ~expected diagnostics#)))))

(s/fdef expect-match
        :args (s/cat :expected (s/or :nil nil? :vector #(vector? (drop-quote %)))
                     :s any?
                     :config (s/? (s/or :nil nil?
                                        :sym #(symbol? (drop-quote %))
                                        :map #(map? (drop-quote %)))))
        :ret any?)
