(ns generate-docs
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pp]
    [clojure.set :as set]
    [clojure.string :as str]
    [noahtheduke.spat]
    [noahtheduke.spat.config :refer [default-config]]
    [noahtheduke.spat.rules :refer [global-rules]]))

(defn get-config [rule]
  (@default-config (:full-name rule)))

(defn print-table [ks maps]
  (-> (->> maps
           (map #(set/rename-keys % ks))
           (pp/print-table (vals ks))
           (with-out-str))
      (str/replace "-+-" " | ")
      (str/replace "|-" "| ")
      (str/replace "-|" " |")
      (str/trim)))

(defn render-details
  [rule]
  (print-table {:enabled "Enabled" :added "Added"} [(get-config rule)]))

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
          [docs examples] (str/split lines #"\n\n\s*Examples:")]
      (str (str/trim docs)
           \newline \newline
           "### Examples:"
           \newline
           (when examples
             (str "```clojure"
                  \newline
                  (str/trim examples)
                  \newline
                  "```"))))))

(defn render-reference [rule]
  (let [config (get-config rule)]
    (when-let [style-ref (:style-ref config)]
      (str "### Reference"
           \newline
           "* https://guide.clojure.style/"
           style-ref))))

(defn build-rule [rule]
  (->> [(str "## " (:full-name rule))
        (render-details rule)
        (render-docstring rule)
        (render-reference rule)]
       (remove str/blank?)
       (str/join (str \newline \newline))))

(defn build-rules [genre]
  (->> genre
       (get (group-by :genre (mapcat vals (vals @global-rules))))
       (sort-by :full-name)
       (map build-rule)
       (remove str/blank?)
       (str/join (str \newline \newline))))

(defn genre-page [genre]
  (let [page-title (str "# " (str/capitalize genre))
        rule-sections (build-rules genre)]
    (->> [page-title
          rule-sections]
         (str/join (str \newline \newline)))))

(defn print-genre-to-file [genre]
  (let [page (genre-page genre)
        filename (io/file "docs" "rules" (str genre ".md"))]
    (io/make-parents filename)
    (println "* saving" (str filename))
    (spit filename (str page \newline))))

(defn run [& {:keys [genres]}]
  (when-let [genres (seq (or genres (set (keep :genre (mapcat vals (vals @global-rules))))))]
    (println "Saving genres to file")
    (run! print-genre-to-file genres)))
