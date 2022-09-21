; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns user
  (:require [hyperfiddle.rcf :as rcf]
            [clojure.tools.namespace.repl :as tns]
            [clojure.tools.deps.alpha.repl :as ctd.repl]))

(def add-libs ctd.repl/add-libs)
(def refresh-all tns/refresh-all)

(rcf/enable!)
