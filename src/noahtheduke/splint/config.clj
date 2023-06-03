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

(def version (delay (str/trim (slurp (io/resource "SPLINT_VERSION")))))
(defn splint-version [] (str "splint v" @version))

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
   (let [merged-options
         (-> (deep-merge @default-config local)
             (set/rename-keys {'output :output
                               'parallel :parallel
                               'summary :summary
                               'quiet :quiet
                               'silent :silent})
             (merge options))]
     ;; Defaults are set here because cli options are merged in last and
     ;; tools.cli defaults can't be distinguished.
     (conj {:parallel true
            :output "full"
            :summary true} merged-options))))

(defn get-config [ctx rule]
  (let [full-name (:full-name rule)
        init-type (:init-type rule)]
    (-> ctx init-type full-name :config)))

(defn spit-config [{:keys [diagnostics]}]
  (let [diagnostic-counts (update-vals (group-by :rule-name diagnostics) count)
        rules (reduce-kv
                (fn [m k v]
                  (str m (format "\n ;; Diagnostics count: %s\n %s\n {:description %s\n  :enabled false}\n"
                                 v
                                 (str k)
                                 (-> @default-config k :description pr-str))))
                ""
                (into (sorted-map) diagnostic-counts))
        new-config (str/join
                     "\n"
                     [(str ";; Splint configuration auto-generated on "
                           (.format (java.text.SimpleDateFormat. "yyyy-MM-dd")
                                    (java.util.Date.)))
                      ""
                      "{"
                      (str " " (str/trim rules))
                      "}"])]
    (spit ".splint.edn" new-config)))
