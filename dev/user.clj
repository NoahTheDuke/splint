; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns user
  (:require
    [clojure.tools.namespace.repl :as tns]
    [clj-java-decompiler.core :as decompiler]
    [criterium.core :as criterium]
    [clojure.java.io :as io]))

(defn refresh-all [& opts] (apply tns/refresh-all opts))
(defmacro decompile [form] `(decompiler/decompile ~form))
(defmacro quick-bench [expr & opts] `(criterium/quick-bench ~expr ~@opts))
(defmacro bench [expr & opts] `(criterium/quick-bench ~expr ~@opts))


(doseq [dev-rule (file-seq (io/file "dev" "rules" "dev"))
        :when (.isFile dev-rule)]
  (load-file (str dev-rule)))
