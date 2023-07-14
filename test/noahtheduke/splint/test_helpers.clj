; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    matcher-combinators.test
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [expectations.clojure.test :refer [expect]]
    [matcher-combinators.core :refer [Matcher]]
    [matcher-combinators.result :as-alias result]
    [noahtheduke.splint.config :refer [merge-config]]
    [noahtheduke.splint.dev :as dev]
    [noahtheduke.splint.parser :refer [parse-file]]
    [noahtheduke.splint.runner :refer [run-impl]]
    [noahtheduke.splint.utils :refer [drop-quote]])
  (:import
    (java.io File)
    (java.nio.file Files)
    (java.nio.file.attribute FileAttribute)))

(set! *warn-on-reflection* true)

(defn file-match [^File this actual]
  (cond
    (string? actual)
    (let [this-path (str/split (.getAbsolutePath this) (re-pattern File/separator))
          actual-path (str/split (.getAbsolutePath (io/file actual)) #"/")]
      (if (= this-path actual-path)
        {::result/type :match
         ::value actual
         ::weight 0}
        {::result/type :mismatch
         ::value actual
         ::weight 1}))
    (instance? File actual)
    (if (.equals (.getName this) (.getName ^File actual))
      {::result/type :match
       ::value actual
       ::weight 0}
      {::result/type :mismatch
       ::value actual
       ::weight 1})
    :else
    {::result/type :mismatch
     ::value actual
     ::weight 1}))

(extend-protocol Matcher
  File
  (-matcher-for
    ([this] this)
    ([this _] this))
  (-name [_] 'file-match)
  (-match [this actual] (file-match this actual)))

(defn check-all
  ([path] (check-all path nil))
  ([path config]
   (let [config (conj {:dev true
                       :clojure-version (or (:clojure-version config)
                                            *clojure-version*)}
                      (merge-config @dev/dev-config config))
         results (run-impl path config)]
     (seq (:diagnostics results)))))

(defmacro expect-match
  ([expected s] `(expect-match ~expected ~s nil))
  ([expected s config]
   `(let [diagnostics# (check-all ~s ~config)]
      (expect (~'match? ~expected diagnostics#)))))

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
  [bindings & body]
  (let [paths (take-nth 2 (drop 1 bindings))
        temp-dir (gensym)
        temp-files (map
                     (fn [path]
                       [(gensym)
                        `(Files/createFile (.toPath (io/file (str ~temp-dir) ~path))
                                           (into-array FileAttribute []))])
                     paths)
        binds (mapcat (fn [b f] [b f])
                      (take-nth 2 bindings)
                      (map (fn [[path _]] `(io/file (str ~path)))
                           temp-files))]
    `(let [~temp-dir (Files/createTempDirectory
                       "splint" (into-array FileAttribute []))
           ~@(mapcat identity temp-files)
           ~@binds]
       (try (let [res# (do ~@body)] res#)
            (finally
              (doseq [f# ~(mapv first temp-files)]
                (Files/deleteIfExists f#))
              (Files/deleteIfExists ~temp-dir))))))

(defmacro print-to-file!
  "Print "
  [file & body]
  `(with-open [~file (io/writer ~file)]
     (binding [*out* ~file]
       ~@(map #(list `println %) body))))
