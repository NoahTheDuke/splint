; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.set :as set]
    [clojure.string :as str]))

(set! *warn-on-reflection* true)

(def version (str/trim (slurp "./resources/SPLINT_VERSION")))
(def splint-version (str "splint v" version))

(defn read-default-config []
  (edn/read-string (slurp (io/resource "config/default.edn"))))

(def default-config
  (delay (read-default-config)))

(defn find-local-config []
  (loop [dir (.getParentFile (.getAbsoluteFile (io/file ".")))]
    (let [config (io/file dir ".splint.edn")]
      (if (.exists config)
        {:dir dir
         :file (.getAbsoluteFile config)
         :local (edn/read-string (slurp config))}
        (when-let [parent (.getParentFile dir)]
          (recur parent))))))

(defn deep-merge [default & maps]
  (letfn [(reconcile-keys [val-in-result val-in-latter]
            (if (and (map? val-in-result)
                     (map? val-in-latter))
              (merge-with reconcile-keys val-in-result val-in-latter)
              val-in-latter))
          (reconcile-maps [result latter]
            (merge-with reconcile-keys result latter))]
    (reduce reconcile-maps default maps)))

(defn load-config
  ([options] (load-config (:local (find-local-config)) options))
  ([local options]
   (-> (deep-merge @default-config local)
       (set/rename-keys {'output :output
                         'parallel :parallel
                         'quiet :quiet})
       (merge options))))

(defn get-config [ctx rule]
  (let [full-name (:full-name rule)
        init-type (:init-type rule)]
    (-> ctx init-type full-name :config)))
