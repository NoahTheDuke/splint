; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns build
  (:require
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as dd]))

(def lib 'io.github.noahtheduke/splint)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "classes")

(defn compile- [opts]
  (println "Compiling" lib)
  (b/delete {:path "classes"})
  (let [basis (b/create-basis {:aliases [:dev]})]
    (b/compile-clj {:basis basis
                    :class-dir class-dir}))
  opts)

(defn run-tests "Run all the tests." [opts]
  (compile- opts)
  (println "Running tests")
  (let [basis (b/create-basis {:aliases [:test :kaocha]})
        cmd (b/java-command
              {:basis      basis
               :main      'clojure.main
               :main-args ["-m" "kaocha.runner"]})
        {:keys [exit]} (b/process cmd)]
    (if (pos? exit)
      (System/exit exit)
      opts)))

(defn make-opts [opts]
  (assoc opts
         :lib lib
         :version version
         :basis (b/create-basis {})
         :scm {:tag (str "v" version)}
         :jar-file (format "target/%s-%s.jar" (name lib) version)
         :class-dir class-dir
         :src-dirs ["src"]
         :resource-dirs ["resources"]
         :uber-file (format "target/%s-%s-standalone.jar" (name lib) version)
         :main 'noahtheduke.splint))

(defn uber [opts]
  (let [opts (make-opts opts)]
    (b/delete {:path "target"})
    (b/copy-dir {:src-dirs ["src" "resources"]
                 :target-dir class-dir})
    (b/compile-clj opts)
    (b/uber opts)))

(defn deploy [opts]
  (let [opts (make-opts opts)]
    (b/write-pom opts)
    (b/copy-dir {:src-dirs ["src" "resources"]
                 :target-dir class-dir})
    (b/jar opts)
    (dd/deploy {:installer :remote
                :artifact (b/resolve-path (:jar-file opts))
                :pom-file (b/pom-path opts)})))
