; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [edamame.core :as e]
   [noahtheduke.splint.clojure-ext.core :refer [mapv*]]
   [noahtheduke.splint.path-matcher :refer [->matcher]]
   [noahtheduke.splint.rules :refer [global-rules]])
  (:import
   (java.io File)))

(set! *warn-on-reflection* true)

(def version (delay (str/trim (slurp (io/resource "SPLINT_VERSION")))))
(defn splint-version [] (str "splint v" @version))

(defn slurp-edn
  "Read values from an edn file.

  Returns nil if file is empty.
  If :multiple is provided, returns a vector of values.
  If :multiple is not provided, returns a single value or throws if file contains more than 1 value."
  ([file] (slurp-edn file nil))
  ([file {:keys [multiple]}]
   (let [v (e/parse-string-all (slurp file) {:all true})]
     (cond
       multiple (not-empty v)
       (< 1 (count v))
       (throw (ex-info "edn file has more than 1 value" {:file file
                                                         :type :config}))
       :else
       (first v)))))

(defn read-default-config []
  (slurp-edn (io/resource "config/default.edn")))

(def default-config
  (delay (read-default-config)))

(defn find-local-config []
  (loop [dir (.getParentFile (.getAbsoluteFile (io/file ".")))]
    (let [config (io/file dir ".splint.edn")]
      (if (.exists config)
        {:dir dir
         :file (.getAbsoluteFile config)
         :local (slurp-edn config)}
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
        silent (config 'silent (config :silent false))
        autocorrect (config :autocorrect false)]
    {:output output
     :parallel parallel
     :summary summary
     :quiet quiet
     :silent silent
     :autocorrect autocorrect}))

(defn make-rule-config [rule genre-config local-config]
  (let [combined-rule
        (cond-> rule
          genre-config (conj genre-config)
          local-config (conj local-config))
        combined-rule
        (cond-> combined-rule
          (seq (:includes combined-rule))
          (update :includes #(mapv* ->matcher %))
          (seq (:excludes combined-rule))
          (update :excludes #(mapv* ->matcher %)))]
    combined-rule))

(defn merge-config
  "Merge the local config file into the default.

  Applies whole genre configuration to each applicable rule, and merges all
  custom rules configuration as well.

  If .splint.edn has both `'output` and `:output`, it will use `'output`."
  [default local]
  (let [default (or default {})
        ;; Select all settings that apply globally
        global (make-rule-config ('global local {}) nil nil)
        ;; Select whole genres from local config
        whole-genres (select-keys local (:genres @global-rules))
        ;; Select non-opts, non-genres
        local-rules (into {} (filter (comp qualified-symbol? key)) local)
        ;; For each loaded rule:
        ;; * Merge (left to right) the default config,
        ;;   the whole genre config, and the local config for that rule.
        ;; * Add the merged rule config into the new config.
        new-config (->> (:rules @global-rules)
                     (keys)
                     (reduce
                       (fn [m rule-name]
                         (let [genre (symbol (namespace rule-name))
                               genre-config (whole-genres genre)
                               local-config (or (local-rules rule-name) {})
                               rule-config (make-rule-config
                                            (assoc (default rule-name) :rule-name rule-name)
                                            genre-config
                                            local-config)]
                           (assoc! m rule-name rule-config)))
                       (transient {}))
                     (persistent!))
        new-config (-> {:global global}
                     (conj local-rules)
                     (conj new-config))
        ;; Merge in the cli opts to the new config.
        opts (get-opts-from-config local)]
    (conj new-config opts)))

(defn- require-file! [f]
  (try (load-file f)
       (catch java.io.FileNotFoundException _
         (println "Can't load" f "as it doesn't exist.")))
  f)

(defn require-files! [local options]
  (->> (:required-files options)
       (concat (:require local ('require local)))
       (mapv* require-file!)
       (not-empty)))

(defn load-config
  ([options] (load-config (:local (find-local-config)) options))
  ([local options]
   (let [required-files (require-files! local options)]
     (conj (merge-config @default-config local)
           options
           {:required-files required-files}))))

(defn get-config
  "Return merged config for a specific rule."
  [ctx rule-name]
  (-> ctx :rules rule-name :config))

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

(defn parse-clojure-version
  [version]
  (let [pat #"(?<major>\d+)\.(?<minor>\d+)\.(?<incremental>\d+)(?:-(?<qualifier>[a-zA-Z0-9_]+))?(?:-(?<snapshot>SNAPSHOT))?"
        m (re-matcher pat version)
        _ (.matches m)
        qualifier (.group m "qualifier")
        snapshot (if (.equals "SNAPSHOT" qualifier)
                   qualifier
                   (.group m "snapshot"))
        qualifier (when-not (.equals "SNAPSHOT" qualifier)
                    qualifier)]
    {:major (parse-long (.group m "major"))
     :minor (parse-long (.group m "minor"))
     :incremental (parse-long (.group m "incremental"))
     :qualifier qualifier
     :snapshot snapshot}))

(defn project-clojure-version
  [project-map]
  (if project-map
    (when-let [version-str
               (case (::type project-map)
                 :deps-edn (some-> (:deps project-map)
                             ('org.clojure/clojure)
                             :mvn/version)
                 :project-clj (some->> (:dependencies project-map)
                                (filter #(= 'org.clojure/clojure (first %)))
                                first
                                second)
                 ; else
                 nil)]
      (parse-clojure-version version-str))
    *clojure-version*))

(defn project-paths
  "All of the default paths in the project map.
  For deps.edn, :paths from the base plus :paths or :extra-paths from :dev and :test aliases.
  For project.clj, :source-paths and :test-paths from base plus both from :dev and :test profiles."
  [project-map]
  (when project-map
    (let [src-paths (case (::type project-map)
                      :deps-edn (:paths project-map)
                      :project-clj (into (or (:source-paths project-map)
                                           ["src"])
                                     (or (:test-paths project-map)
                                       ["test"]))
                      ; else
                      nil)
          other-paths (case (::type project-map)
                        :deps-edn (when-let [aliases (:aliases project-map)]
                                    (into (some-> aliases :dev :extra-paths)
                                      (some-> aliases :test :extra-paths)))
                        :project-clj
                        (when-let [profiles (:profiles project-map)]
                          (concat
                            (when (map? (:dev profiles))
                              (into (some-> profiles :dev :source-paths)
                                (some-> profiles :dev :test-paths)))
                            (when (map? (:test profiles))
                              (into (some-> profiles :test :source-paths)
                                (some-> profiles :test :test-paths)))))
                        ; else
                        nil)]
      (into (vec src-paths) other-paths))))

(defn read-project-file
  "Read the first available proejct config file.
  In order, checks deps.edn, project.clj, boot."
  [^File deps-edn ^File project-clj]
  (let [project-file
        (cond
          (and deps-edn (.exists deps-edn))
          (assoc (slurp-edn deps-edn) ::type :deps-edn)
          (and project-clj (.exists project-clj))
          (let [v (->> (slurp-edn project-clj {:multiple true})
                    (filter #(and (seq? %) (= 'defproject (first %))))
                    first
                    (drop 3)
                    (apply hash-map))]
            (assoc v ::type :project-clj)))]
    {:clojure-version (project-clojure-version project-file)
     :paths (project-paths project-file)}))
