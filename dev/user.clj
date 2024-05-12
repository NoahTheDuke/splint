; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns user
  (:require
   [clj-java-decompiler.core :as decompiler]
   [clojure.main :as main]
   [clojure.tools.namespace.repl :as tns]
   [criterium.core :as criterium]
   [noahtheduke.splint.dev]
   [potemkin :refer [import-vars]]
   clojure.repl
   [taoensso.tufte :as tufte]))

(set! *warn-on-reflection* true)

(apply require main/repl-requires)

(import-vars
  [clojure.tools.namespace.repl
   refresh-all]
  [clj-java-decompiler.core
   decompile]
  [criterium.core
   quick-bench
   bench]
  [taoensso.tufte
   p
   profile])
