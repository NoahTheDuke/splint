; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [noahtheduke.splint.rules :refer [global-rules]])
  (:import
    (java.io File FileReader PushbackReader)))

(set! *warn-on-reflection* true)

(def version (delay (str/trim (slurp (io/resource "SPLINT_VERSION")))))
(defn splint-version [] (str "splint v" @version))

(defn slurp-1-edn
  "Read a single value from an edn file. Returns nil if file is empty, throws if file contains more than 1 value."
  [^File file]
  (let [eof (Object.)
        file-reader (FileReader. file)]
    (with-open [rdr (PushbackReader. file-reader)]
      (let [v (edn/read {:eof eof} rdr)]
        (when-not (identical? eof v)
          (if (identical? eof (edn/read {:eof eof} rdr))
            v
            (throw (ex-info "edn file has more than 1 value" {:file file
                                                              :type :config}))))))))

(defn read-default-config []
  (slurp-1-edn (io/file (io/resource "config/default.edn"))))

(def default-config
  (delay (read-default-config)))

(defn find-local-config []
  (loop [dir (.getParentFile (.getAbsoluteFile (io/file ".")))]
    (let [config (io/file dir ".splint.edn")]
      (if (.exists config)
        {:dir dir
         :file (.getAbsoluteFile config)
         :local (slurp-1-edn config)}
        (when-let [parent (.getParentFile dir)]
          (recur parent))))))

(defn get-opts-from-config
  [config]
  ;; Defaults are set here because cli options are merged in last and
  ;; tools.cli defaults can't be distinguished.
  (let [config (or config {})
        output (config 'output (config :output "full"))
        parallel (config 'parallel (config :parallel true))
        summary (config 'summary (config :summary true))
        quiet (config 'quiet (config :quiet false))
        silent (config 'silent (config :silent false))]
    {:output output
     :parallel parallel
     :summary summary
     :quiet quiet
     :silent silent}))

(defn merge-config
  "Merge the local config file into the default.

  Applies whole genre configuration to each applicable rule, and merges all
  custom rules configuration as well.

  If .splint.edn has both `'output` and `:output`, it will use `'output`."
  [default local]
  (let [;; Select whole genres from local config
        whole-genres (select-keys local (map symbol (:genres @global-rules)))
        ;; Select non-opts, non-genres as a `volatile!`
        local-rules (into {} (filter (comp qualified-symbol? key)) local)
        ;; For each rule in the defaults:
        ;; * Merge (left to right) the default config,
        ;;   the whole genre config, and the local config for that rule.
        ;; * Remove that rule's entry from the local-rules map.
        ;; * Add the merged rule config into the new config.
        new-config (->> default
                        (reduce-kv
                          (fn [m k v]
                            (let [genre (symbol (namespace k))
                                  genre-config (whole-genres genre)
                                  local-config (local-rules k)
                                  rule-config (cond-> (assoc v :rule-name k)
                                                genre-config (conj genre-config)
                                                local-config (conj local-config))]
                              (assoc! m k rule-config)))
                          (transient {}))
                        (persistent!))
        ;; If there are custom local rules that aren't in the defaults,
        ;; just merge them directly into the new config.
        new-config (conj local-rules new-config)
        ;; Merge in the cli opts to the new config.
        opts (get-opts-from-config local)]
    (conj new-config opts)))

(defn load-config
  ([options] (load-config (:local (find-local-config)) options))
  ([local options]
   (conj (merge-config @default-config local)
         options)))

(defn get-config [_ctx rule]
  (:config rule))

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
