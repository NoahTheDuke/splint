; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns generate-docs
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [doric.core :refer [table]]
   [noahtheduke.splint]
   [noahtheduke.splint.config :refer [default-config]]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.utils :refer [simple-type]]))

(defn rule-config [rule]
  (@default-config (:full-name rule)))

(defn print-table
  [headers maps]
  (-> (table headers maps)
    (str/split-lines)
    (rest)
    (butlast)
    (->> (str/join "\n"))
    (str/replace "-+-" " | ")
    (str/replace "|-" "| ")
    (str/replace "-|" " |")
    (str/trim)))

(defn render-details
  [rule]
  (let [headers [{:name :enabled :title "Enabled by default" :align :left}
                 {:name :added :title "Version Added" :align :left}
                 {:name :updated :title "Version Updated" :align :left}]]
    (print-table headers [(rule-config rule)])))

(defn render-version-note [rule]
  (when-let [min-clojure-version (:min-clojure-version rule)]
    (let [min-clojure-version (conj {:major 1 :minor 9 :incremental 0}
                                min-clojure-version)]
      (binding [*clojure-version* min-clojure-version]
        (format "**NOTE**: Requires Clojure version %s."
          (clojure-version))))))

(defn render-docstring [rule]
  (when-let [docstring (:docstring rule)]
    (let [lines (str/split-lines docstring)
          min-indent (some->> (next lines)
                       (remove str/blank?)
                       (seq)
                       (map #(- (count %) (count (str/triml %))))
                       (apply min))
          dedented-lines (map #(if (str/blank? %) % (subs % min-indent)) (next lines))
          lines (str/join \newline (cons (first lines) dedented-lines))
          [docs examples] (str/split lines #"Examples:")]
      (str (str/trim docs)
        (when examples
          (str \newline \newline
            "### Examples"
            \newline \newline
            "```clojure"
            \newline
            (str/trim examples)
            \newline
            "```"))))))

(defn build-styles [rule]
  (let [config (rule-config rule)
        chosen-style (:chosen-style config)
        supported-style (:supported-styles config)]
    (when (and (or chosen-style supported-style)
            (not (and chosen-style supported-style)))
      (throw (ex-info "Need both chosen-style and supported-style" {:rule (:full-name rule)})))
    (when (and chosen-style supported-style)
      [{:name "`:chosen-style`"
        :default (str "`" chosen-style "`")
        :options (->> supported-style
                   (map #(str "`" % "`"))
                   (str/join ", "))}])))

(defn build-other-configs [rule]
  (let [config (rule-config rule)
        opts (-> config
               (dissoc :description :enabled
                 :added :updated
                 :guide-ref :links
                 :chosen-style :supported-styles)
               (not-empty))]
    (when opts
      (mapv (fn [[k v]]
              {:name (str "`" k "`")
               :default (str "`" v "`")
               :options (str/capitalize (name (simple-type v)))})
        opts))))

(defn render-configuration [rule]
  (let [styles (build-styles rule)
        other-configs (build-other-configs rule)
        config (seq (concat styles other-configs))]
    (when config
      (str "### Configurable Attributes"
        \newline \newline
        (print-table [{:name :name :title "Name" :align :left}
                      {:name :default :title "Default" :align :left}
                      {:name :options :title "Options" :align :left}]
          config)))))

(defn render-reference [rule]
  (let [config (rule-config rule)
        guide-ref (:guide-ref config)
        outside-links (seq (:links config))]
    (when (or guide-ref outside-links)
      (str "### Reference"
        \newline \newline
        (when guide-ref
          (format "* https://guide.clojure.style/%s" guide-ref))
        (when (and guide-ref outside-links)
          \newline)
        (when outside-links
          (->> (for [link outside-links]
                 (format "* <%s>" link))
               (str/join \newline)))))))

(defn build-rule [rule]
  (->> [(str "## " (:full-name rule))
        (render-details rule)
        (render-version-note rule)
        (render-docstring rule)
        (render-configuration rule)
        (render-reference rule)]
    (remove str/blank?)
    (str/join (str \newline \newline))))

(def grouped-genres
  (group-by :genre (vals (:rules @global-rules))))

(defn build-rules [genre]
  (->> (grouped-genres genre)
    (sort-by :full-name)
    (map build-rule)
    (interpose "---")
    (remove str/blank?)
    (str/join (str \newline \newline))))

(defn genre-page [genre]
  (->> [(str "# " (str/capitalize genre))
        (build-rules genre)]
    (str/join (str \newline \newline))))

#_{:splint/disable [naming/conversion-functions]}
(defn print-genre-to-file [genre]
  (let [page (genre-page genre)
        filename (io/file "docs" "rules" (str genre ".md"))]
    (io/make-parents filename)
    (println "* saving" (str filename))
    (spit filename (str page \newline))))

(defn -main [& genres]
  (when-let [genres (seq (or genres (:genres @global-rules)))]
    (println "Saving genres to file")
    (run! print-genre-to-file genres)))
