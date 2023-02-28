(ns noahtheduke.spat.config 
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(def default-config
  (delay (edn/read-string (slurp (io/resource "config/default.edn")))))
