; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  "Tests of Splint's own code base: sorting, etc."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.runner :as splint :refer [prepare-rules]]
   [noahtheduke.splint.test-helpers :refer [with-out-str-data-map]]
   [noahtheduke.splint.config :as conf]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.parser :refer [parse-file]])
  (:import
   (java.io File)))

(set! *warn-on-reflection* true)

(defdescribe dogfooding-test
  (it "no diagnostics in splint"
    (match? {:result {:diagnostics []
                      :exit 0}}
      (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
        (with-out-str-data-map)
          ((fn [{results :result}]
             {:result {:diagnostics (:diagnostics results)
                       :exit (:exit results)}}))))))

(defdescribe splint-clj-test
  (let [requires (->> (parse-file {:contents (slurp "src/noahtheduke/splint.clj")})
                      (first) ; ns form
                      (last) ; rules :require
                      (next) ; drop :require
                      ,)]
    (it "lists every rule"
      (expect (= requires (sort requires))
        "Rules in `noahtheduke.splint` must be in sorted order."))
    (it "has all of the rules in the rules config"
      (let [rule-names (->> requires
                            (map str)
                            (map #(take-last 2 (str/split % #"\.")))
                            (map #(symbol (first %) (last %))))
            config-rule-names (-> @conf/default-config
                                  (assoc :clojure-version {:major 99})
                                  (prepare-rules (:rules @global-rules))
                                  :rules
                                  (->> (remove #(.equals "dev" (:genre (val %))))
                                       (map (comp str key))
                                       (map #(str/replace % "?" ""))
                                       (map symbol)))]
        (expect (= (set rule-names) (set config-rule-names)))))))

(defdescribe config-test
  (it "has sorted default config"
    (let [default-config (slurp (io/resource "config/default.edn"))
          config-as-vec (->> default-config
                             (str/split-lines)
                             (drop-while #(not= \{ (first %)))
                             (str/join \newline))
          config-as-vec (edn/read-string
                         (str "[" (subs config-as-vec 1 (dec (count config-as-vec))) "]"))
          config-keys (take-nth 2 config-as-vec)]
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
  (it "all files have license headers"
    (let [files (->> ["dev" "resources" "src" "test"]
                     (mapcat #(file-seq (io/file %)))
                     (filter #(and (.isFile ^File %)
                                   (some (fn [ft] (str/ends-with? % ft))
                                         [".clj" ".cljs" ".cljc" ".edn"]))))]
      (doseq [file files
              :let [f-str (slurp file)]]
        (let [result (if (adapted-files (str file))
                       (str/starts-with? f-str "; Adapted from")
                       (str/starts-with? f-str mpl-v2))]
          (expect result (str file " doesn't start with a license")))))))
