; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'io.github.noahtheduke/spat)
(def version "1.0.0")

(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir "target/classes"})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir "target/classes"})
  (b/uber {:class-dir "target/classes"
           :uber-file uber-file
           :basis basis
           :main 'noahtheduke.spat}))
