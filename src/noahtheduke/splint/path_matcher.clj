; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher
  (:require
   [clojure.string :as str])
  (:import
   (java.io File)
   (java.nio.file FileSystem FileSystems PathMatcher)
   [java.util.regex Matcher]))

(set! *warn-on-reflection* true)

(defonce ^FileSystem fs (FileSystems/getDefault))

(defprotocol Match
  (-matches [matcher file-or-path] "Check if file-or-path matches pattern."))

(defrecord MatchHolder [pattern input])

(extend-protocol Match
  PathMatcher
  (-matches [m file]
    (PathMatcher/.matches m (File/.toPath ^File file)))
  java.util.regex.Pattern
  (-matches [m file]
    (let [m (re-matcher m (str file))]
      (Matcher/.find m)))
  String
  (-matches [m file]
    (str/includes? (str file) m)))

(defn matches [matcher file]
  (-matches (:pattern matcher) file))

(defn re-find-matcher [input]
  (re-pattern (str/replace-first input "re-find:" "")))

(defn ->matcher [input]
  (cond
    (or (str/starts-with? input "glob:")
      (str/starts-with? input "regex:"))
    (->MatchHolder (FileSystem/.getPathMatcher fs input) input)
    (str/starts-with? input "re-find:")
    (->MatchHolder (re-find-matcher input) input)
    (str/starts-with? input "string:")
    (->MatchHolder (str/replace-first input "string:" "") input)
    :else
    (->MatchHolder (re-find-matcher input) (str "re-find:" input))))
