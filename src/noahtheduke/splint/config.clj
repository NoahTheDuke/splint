; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
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

(defn merge-config [given new]
  (reduce-kv
    (fn [m k v]
      (cond
        (map? v)
        (update m k conj (dissoc v :description :added :updated :supported-styles))
        (case k
          (output :output
           parallel :parallel
           summary :summary
           quiet :quiet
           silent :silent)
          true false) (assoc m (keyword k) v)
        :else m))
    given
    new))

(defn load-config
  ([options] (load-config (:local (find-local-config)) options))
  ([local options]
   (let [merged-options (-> (merge-config @default-config local)
                            (conj options))]
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
  (let [rule-strs (->> (group-by :rule-name diagnostics)
                       (into (sorted-map))
                       (reduce-kv
                         (fn [m rule-name diagnostics]
                           (conj m (str " ;; Diagnostics count: " (count diagnostics)
                                        "\n ;; " (-> @default-config rule-name :description)
                                        (when-let [supported-styles (-> @default-config rule-name :supported-styles)]
                                          (str "\n ;; :supported-styles " (pr-str supported-styles)))
                                        "\n " (str rule-name) " {:enabled false}")))
                         []))
        new-config (str/join
                     "\n"
                     [(str ";; Splint configuration auto-generated on "
                           (.format (java.text.SimpleDateFormat. "yyyy-MM-dd")
                                    (java.util.Date.)) ".")
                      ";; All failing rules have been disabled and can be enabled as time allows."
                      ""
                      "{"
                      (str " " (str/trim (str/join "\n\n" rule-strs)))
                      "}"])]
    (spit ".splint.edn" new-config)))
