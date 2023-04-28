; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.conventional-aliases-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.test-helpers :refer [check-all]]))

(defexpect conventional-aliases-test
  #_(expect '(:require [clojure.string :as str])
    (-> (check-all "(ns foo.bar (:require [clojure.string :as string]))")
        (first)
        (:alt)))
  (expect '(:require [clojure.string :as str]
                     clojure.set
                     [clojure.edn]
                     [clojure pprint [zip :refer [1 2 3] :as zip]
                      [edn :as edn]])
    (-> (check-all "(ns foo.bar
                      (:require [clojure.string :as string]
                        clojure.set
                        [clojure.edn]
                        [clojure pprint [zip :refer [1 2 3] :as z]
                        [edn :as e]]))")
        (first)
        (:alt))))
