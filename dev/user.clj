; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns user
  (:require
    [clj-java-decompiler.core :as decompiler]
    [clojure.java.io :as io]
    [clojure.tools.namespace.repl :as tns]
    [criterium.core :as criterium]
    [nextjournal.beholder :as beholder]
    [noahtheduke.splint.config :as config]
    [noahtheduke.splint.test-helpers :as test-helpers]))

(defn refresh-all [& opts] (apply tns/refresh-all opts))
(defmacro decompile [form] `(decompiler/decompile ~form))
(defmacro quick-bench [expr & opts] `(criterium/quick-bench ~expr ~@opts))
(defmacro bench [expr & opts] `(criterium/quick-bench ~expr ~@opts))


(doseq [dev-rule (file-seq (io/file "dev" "rules" "dev"))
        :when (.isFile dev-rule)]
  (load-file (str dev-rule)))

(def watcher
  (beholder/watch
    (fn [action]
      (when (#{:create :modify} (:type action))
        (reset! test-helpers/default-config (config/read-default-config))))
    "resources"))

(comment
  (beholder/stop watcher))
