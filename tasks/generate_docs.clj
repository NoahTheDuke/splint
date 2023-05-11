(ns generate-docs
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [doric.core :refer [table]]
    [noahtheduke.splint]
    [noahtheduke.splint.config :refer [default-config]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.spat.pattern :refer [simple-type]]))

(defn get-config [rule]
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
   (print-table headers
                [(get-config rule)])))

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
  (let [config (get-config rule)
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
  (let [config (get-config rule)
        opts (-> config
                 (dissoc :description :enabled
                         :added :updated
                         :guide-ref :link
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
  (let [config (get-config rule)
        guide-ref (:guide-ref config)
        outside-link (:link config)]
    (when (or guide-ref outside-link)
      (str "### Reference"
           \newline \newline
           (when guide-ref
             (format "* https://guide.clojure.style/%s" guide-ref))
           (when (and guide-ref outside-link)
             \newline)
           (when outside-link
             (format "* <%s>" outside-link))))))

(defn build-rule [rule]
  (->> [(str "## " (:full-name rule))
        (render-details rule)
        (render-docstring rule)
        (render-configuration rule)
        (render-reference rule)]
       (remove str/blank?)
       (str/join (str \newline \newline))))

(defn build-rules [genre]
  (->> genre
       (get (group-by :genre (vals @global-rules)))
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
  (when-let [genres (seq (or genres (set (keep :genre (vals @global-rules)))))]
    (println "Saving genres to file")
    (run! print-genre-to-file genres)))
