; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
   matcher-combinators.test
   noahtheduke.splint
   noahtheduke.splint.rules.helpers
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [lazytest.core :refer [expect]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.core :as mc]
   [matcher-combinators.model :refer [->Mismatch]]
   [matcher-combinators.result :as-alias result]
   [noahtheduke.splint.config :refer [merge-config]]
   [noahtheduke.splint.dev :as dev]
   [noahtheduke.splint.parser :refer [parse-file]]
   [noahtheduke.splint.runner :refer [run-impl]]
   [noahtheduke.splint.utils :refer [drop-quote]])
  (:import
   (java.io File)
   (java.nio.file Files FileVisitor FileVisitResult)
   (java.nio.file.attribute FileAttribute)
   (noahtheduke.splint.path_matcher MatchHolder)))

(set! *warn-on-reflection* true)

(defmacro with-out-str-data-map
  "Adapted from clojure.core/with-out-str.

  Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter. Returns the result of the body and the string created by any
  nested printing calls in a map under the respective keys :result and :string."
  [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [*out* s#]
       (let [r# (do ~@body)]
         {:result r#
          :string (str s#)}))))

(defn prep-dev-config [config]
  (conj {:clojure-version (or (:clojure-version config)
                              *clojure-version*)}
        (merge-config @dev/dev-config config)
        {:parallel false}))

(defn check-all
  ([path] (check-all path nil))
  ([path config]
   (let [paths (if (sequential? path) path [path])
         results (run-impl paths (prep-dev-config config))]
     (seq (:diagnostics results)))))

(defmacro expect-match
  ([expected s] `(expect-match ~expected ~s nil))
  ([expected s config]
   `(let [diagnostics# (check-all ~s ~config)
          expected# ~expected]
      (expect (match? expected# diagnostics#)))))

(s/fdef expect-match
  :args (s/cat :expected (s/or :nil nil? :vector #(vector? (drop-quote %)))
          :s any?
          :config (s/? any?))
  :ret any?)

(defn parse-string-all
  "Wrapper around [[parse-file]] to consume a string instead of a file"
  [s]
  (parse-file {:ext :clj :features #{:clj} :contents s}))

(defn parse-string
  "Wrapper around [[parse-file]] to consume a string and return the first form"
  [s]
  (first (parse-string-all s)))

(defmacro with-temp-files
  "Initialize a temp directory with given files, bind the files to the given
  symbols, execute the body with those bound, then delete the directory.
  file-binds are pairs of simple-symbols and strings. The symbols will be
  available in the body, and the strings will be interpreted as file paths
  (with optionally specified parent directories).

  (with-temp-files
    [core \"src/noahtheduke/core.clj\"]
    (spit core \"(ns noahtheduke.core)\"))"
  [file-binds & body]
  (let [paths (take-nth 2 (drop 1 file-binds))
        temp-dir (gensym)
        temp-files (map
                     (fn [path]
                       [(gensym)
                        `(let [f# (io/file (str ~temp-dir) ~path)]
                           (Files/createDirectories (.toPath (io/file (.getParent f#)))
                             (into-array FileAttribute []))
                           (Files/createFile (.toPath f#)
                             (into-array FileAttribute [])))])
                     paths)
        binds (mapcat vector
                (take-nth 2 file-binds)
                (map (fn [[path _]] `(io/file (str ~path)))
                  temp-files))]
    `(let [~temp-dir (Files/createTempDirectory
                       "splint" (into-array FileAttribute []))
           ~@(mapcat identity temp-files)
           ~@binds]
       (try (let [res# (do ~@body)] res#)
         (finally
           (Files/walkFileTree
             ~temp-dir
             #{}
             Integer/MAX_VALUE
             (reify FileVisitor
               (preVisitDirectory [_ dir# attrs#]
                 FileVisitResult/CONTINUE)
               (postVisitDirectory [_ dir# attrs#]
                 (Files/deleteIfExists dir#)
                 FileVisitResult/CONTINUE)
               (visitFile [_ path# attrs#]
                 (Files/deleteIfExists path#)
                 FileVisitResult/CONTINUE)
               (visitFileFailed [_ path# ex#]
                 FileVisitResult/CONTINUE))))))))

(s/def ::binding (s/cat :file-name simple-symbol? :path string?))
(s/def ::bindings (s/and vector? #(even? (count %)) (s/* ::binding)))
(s/fdef with-temp-files
  :args (s/cat :bindings ::bindings
          :body (s/* any?))
  :ret any?)

(defmacro print-to-file!
  "Print the result from each form in body, write them to the file."
  [file & body]
  `(with-open [file# (io/writer ~file)]
     (binding [*out* file#]
       ~@(map #(list `println %) body))))

(defn single-rule-config [rule-name & {:as style}]
  (-> @dev/dev-config
    (update-vals #(assoc % :enabled false))
    (update rule-name assoc :enabled true)
    (cond->
      style (update rule-name merge style))))

(defn file-match [^File this actual]
  (cond
    (instance? File actual)
    (if (.equals (.getName this) (.getName ^File actual))
      {::result/type :match
       ::result/value actual
       ::result/weight 0}
      {::result/type :mismatch
       ::result/value (->Mismatch this actual)
       ::result/weight 1})
    (string? actual)
    (let [this-path (str/split (.getAbsolutePath this) (re-pattern File/separator))
          actual-path (str/split (.getAbsolutePath (io/file actual)) #"/")]
      (if (= this-path actual-path)
        {::result/type :match
         ::result/value actual
         ::result/weight 0}
        {::result/type :mismatch
         ::result/value (->Mismatch this actual)
         ::result/weight 1}))
    :else
    {::result/type :mismatch
     ::result/value (->Mismatch this actual)
     ::result/weight 1}))

(defn path-matcher-match [this actual]
  (cond
    (instance? MatchHolder actual)
    (let [this-pattern (:input this)
          actual-pattern (:input actual)]
      (if (= this-pattern actual-pattern)
        {::result/type :match
         ::result/value actual-pattern
         ::result/weight 0}
        {::result/type :mismatch
         ::result/value (->Mismatch this-pattern actual-pattern)
         ::result/weight 1}))
    (string? actual)
    (let [this-pattern (:input this)]
      (if (= this-pattern actual)
        {::result/type :match
         ::result/value actual
         ::result/weight 0}
        {::result/type :mismatch
         ::result/value (->Mismatch this-pattern actual)
         ::result/weight 1}))
    :else
    {::result/type :mismatch
     ::result/value (->Mismatch (list '->MatchHolder (:input this)) actual)
     ::result/weight 1}))

(extend-protocol mc/Matcher
  File
  (-matcher-for
    ([this] this)
    ([this _] this))
  (-base-name [_] 'file-match)
  (-match [this actual] (file-match this actual))
  MatchHolder
  (-matcher-for
    ([this] this)
    ([this _] this))
  (-base-name [_] 'match-holder-match)
  (-match [this actual] (path-matcher-match this actual)))
