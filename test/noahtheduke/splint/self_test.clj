; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  "Tests of Splint's own code base: sorting, etc."
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.test :refer [is]]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.runner :as splint]
    [noahtheduke.splint.utils.test-runner :refer [with-out-str-data-map]]
    [clojure.spec.alpha :as s]))

(defexpect dogfooding-test
  (expect
    (match? {:result {:diagnostics []
                      :exit 0}}
            (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
                (with-out-str-data-map)))))

(defexpect sorted-default-config-test
  (let [default-config (slurp (io/resource "config/default.edn"))
        config-as-vec (->> default-config
                           (str/split-lines)
                           (drop-while #(not= \{ (first %)))
                           (str/join "\n"))
        config-as-vec (edn/read-string
                        (str "[" (subs config-as-vec 1 (dec (count config-as-vec))) "]"))
        config-keys (take-nth 2 config-as-vec)]
    (is (match? config-keys (sort config-keys)))))

(s/def ::description string?)
(s/def ::enabled boolean?)
(s/def ::added string?)
(s/def ::updated string?)
(s/def ::guide-ref (s/and string? #(str/starts-with? % "#")))
(s/def ::link (s/and string? #(str/starts-with? % "http")))
(s/def ::supported-styles (s/and vector? (s/+ keyword?)))
(s/def ::chosen-style keyword?)

(s/def ::style-pair
  #(if (contains? % :supported-styles)
     (contains? (set (:supported-styles %))
                (:chosen-style %))
     true))

(s/def ::config-key (s/and qualified-symbol? #(#{"lint" "metrics" "naming" "style"} (namespace %))))
(s/def ::config-opts
  (s/and (s/keys :req-un [::description ::enabled ::added ::updated]
                 :opt-un [::guide-ref ::link ::supported-styles ::chosen-style])
         ::style-pair))

(s/def ::default-config
  (s/map-of ::config-key ::config-opts))

(defexpect default-config-spec-test
  (let [default-config (edn/read-string (slurp (io/resource "config/default.edn")))]
    (expect ::default-config default-config)))
