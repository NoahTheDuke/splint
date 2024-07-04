; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.naming.conventional-aliases-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [& [style]]
  (cond-> (single-rule-config 'naming/conventional-aliases)
    style (update 'naming/conventional-aliases merge style)))

(defdescribe conventional-aliases-test
  (it "works"
    (expect-match
      [{:rule-name 'naming/conventional-aliases
        :form '(:require [clojure.string :as string])
        :alt '(:require [clojure.string :as str])}]
      "(ns foo.bar (:require [clojure.string :as string]))"
      (config))
    (expect-match
      [{:rule-name 'naming/conventional-aliases
        :form '(:require [clojure.string :as string]
                         clojure.set
                         [clojure.edn]
                         [clojure pprint [zip :refer [1 2 3] :as z]
                          [edn :as e]])
        :alt '(:require
                [clojure.string :as str]
                clojure.set
                [clojure.edn]
                [clojure pprint [zip :refer [1 2 3] :as zip]
                 [edn :as edn]])}]
      "(ns foo.bar
         (:require [clojure.string :as string]
                   clojure.set
                   [clojure.edn]
                   [clojure pprint [zip :refer [1 2 3] :as z]
                    [edn :as e]]))"
      (config))))
