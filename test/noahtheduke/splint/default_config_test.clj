; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.default-config-test
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [expectations.clojure.test :refer [defexpect expect]]))

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
