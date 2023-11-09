; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher
  (:require
    [clojure.string :as str]) 
  (:import
    (java.io File)
    (java.nio.file FileSystem FileSystems Path PathMatcher)))

(set! *warn-on-reflection* true)

(defonce ^FileSystem fs (FileSystems/getDefault))

(defprotocol Match
  (matches [matcher file-or-path] "Check if file-or-path matches glob."))

(defrecord Matcher [^PathMatcher pm ^String pattern]
  Match
  (matches [_ file-or-path]
    (cond
      (instance? File file-or-path)
      (.matches pm (.toPath ^File file-or-path))
      (instance? Path file-or-path)
      (.matches pm ^Path file-or-path)
      :else
      (throw (IllegalArgumentException.
               (format "%s is not File but %s."
                       file-or-path
                       (type file-or-path)))))))

(defn ->matcher ^Matcher [syntax-and-pattern]
  (if (or (str/starts-with? syntax-and-pattern "glob:")
          (str/starts-with? syntax-and-pattern "regex:"))
    (->Matcher (.getPathMatcher fs syntax-and-pattern) syntax-and-pattern)
    (let [syntax-and-pattern (str "glob:" syntax-and-pattern)]
      (->Matcher (.getPathMatcher fs syntax-and-pattern) syntax-and-pattern))))
