; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'io.github.noahtheduke/splint)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "classes")
(def basis (b/create-basis {:project "deps.edn"}))

(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn uber [_]
  (b/delete {:path "target"})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'noahtheduke.splint}))
