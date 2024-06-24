; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  "Tests of Splint's own code base: sorting, etc."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe expect it given]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.runner :as splint]
   [noahtheduke.splint.utils.test-runner :refer [with-out-str-data-map]])
  (:import
   (java.io File)))

(set! *warn-on-reflection* true)

(defdescribe dogfooding-test
  (it "no diagnostics in splint"
    (match? {:result {:diagnostics []
                      :exit 0}}
      (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
        (with-out-str-data-map)))))

(defdescribe config-test
  (given [default-config (slurp (io/resource "config/default.edn"))
          config-as-vec (->> default-config
                          (str/split-lines)
                          (drop-while #(not= \{ (first %)))
                          (str/join \newline))
          config-as-vec (edn/read-string
                          (str "[" (subs config-as-vec 1 (dec (count config-as-vec))) "]"))
          config-keys (take-nth 2 config-as-vec)]
    (it "has sorted default config"
      (match? config-keys (sort config-keys)))))

(def mpl-v2
  (->> ["; This Source Code Form is subject to the terms of the Mozilla Public"
        "; License, v. 2.0. If a copy of the MPL was not distributed with this"
        "; file, You can obtain one at https://mozilla.org/MPL/2.0/."]
    (str/join \newline)))

(def adapted-files
  #{"src/noahtheduke/splint/parser/defn.clj"
    "src/noahtheduke/splint/parser/ns.clj"
    "test/noahtheduke/splint/utils/test_runner.clj"})

(defdescribe license-test
  (given [files (->> ["dev" "resources" "src" "test"]
                  (mapcat #(file-seq (io/file %)))
                  (filter #(and (.isFile ^File %)
                             (some (fn [ft] (str/ends-with? % ft))
                               [".clj" ".cljs" ".cljc" ".edn"]))))]
    (it "all files have license headers"
      (doseq [file files
              :let [f-str (slurp file)]]
        (let [result (if (adapted-files (str file))
                       (str/starts-with? f-str "; Adapted from")
                       (str/starts-with? f-str mpl-v2))]
          (expect result (str file " doesn't start with a license")))))))
