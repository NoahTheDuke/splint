; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns user
  (:require
   [clj-java-decompiler.core :as decompiler]
   [clojure.repl]
   [clojure.java.javadoc]
   [clojure.pprint]
   [clojure.tools.namespace.repl :as tns]
   [criterium.core :as criterium]
   [noahtheduke.splint.dev]
   [potemkin :refer [import-vars]]
   [taoensso.tufte :as tufte]))

(set! *warn-on-reflection* true)

(import-vars
  [clj-java-decompiler.core decompile]
  [clojure.java.javadoc javadoc]
  [clojure.pprint pp pprint]
  [clojure.repl source apropos dir pst doc find-doc]
  [clojure.tools.namespace.repl refresh-all]
  [criterium.core quick-bench bench]
  [taoensso.tufte p profile])
