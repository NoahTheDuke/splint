; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    matcher-combinators.test
    [matcher-combinators.core :refer [Matcher]]
    [matcher-combinators.result :as-alias result]
    [clojure.spec.alpha :as s]
    [expectations.clojure.test :refer [expect]]
    [noahtheduke.splint.pattern :refer [drop-quote]]
    [noahtheduke.splint.config :refer [merge-config]]
    [noahtheduke.splint.dev :as dev]
    [noahtheduke.splint.runner :refer [run-impl]]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [noahtheduke.splint.parser :refer [parse-file]])
  (:import
    (java.io File)))

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
   (let [config (conj {:dev true} (merge-config @dev/dev-config config))
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
