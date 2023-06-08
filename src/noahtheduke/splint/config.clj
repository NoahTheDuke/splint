; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [noahtheduke.splint.rules :refer [global-rules]]
    [clojure.set :as set]))

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

(defn merge-config
  "Merge the local config file into the default.

  Applies whole genre configuration to each applicable rule, and merges all
  custom rules configuration as well.

  If .splint.edn has both `'output` and `:output`, it will use `'output`."
  [default local]
  (let [;; Normalize cli opts to keywords for internal use.
        local (set/rename-keys local {'output :output
                                      'parallel :parallel
                                      'summary :summary
                                      'quiet :quiet
                                      'silent :silent})
        ;; Select whole genres from local config
        whole-genres (select-keys local (map symbol (:genres @global-rules)))
        ;; Select non-opts, non-genres as a `volatile!`
        local-rules (volatile!
                      (as-> local $
                        (dissoc $ :output :parallel :summary :quiet :silent)
                        (apply dissoc $ whole-genres)))
        ;; For each rule in the defaults:
        ;; * Merge (left to right) the default config,
        ;;   the whole genre config, and the local config for that rule.
        ;; * Remove that rule's entry from the local-rules map.
        ;; * Add the merged rule config into the new config.
        new-config (->> default
                        (reduce-kv
                          (fn [m k v]
                            (let [genre (symbol (namespace k))
                                  rule-config (merge
                                                (assoc v :rule-name k)
                                                (genre whole-genres)
                                                (k @local-rules))]
                              (vswap! local-rules dissoc k)
                              (assoc! m k rule-config)))
                          (transient {}))
                        (persistent!))
        ;; If there are custom local rules that aren't in the defaults,
        ;; just merge them directly into the new config.
        new-config (if (seq @local-rules)
                     (conj new-config @local-rules)
                     new-config)
        ;; Actually merge in the cli opts to the new config.
        opts (select-keys local [:output :parallel :summary :quiet :silent])]
    (conj new-config opts)))

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
