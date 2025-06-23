; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefixed-libspecs-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/prefixed-libspecs)

(defdescribe prefixed-libspecs-test
  (it "handles (require calls)"
    (expect-match
      [{:rule-name rule-name
        :form '(quote [clojure [string :as str] [set :as set]])
        :message "Don't use prefix libspecs in require calls"}]
      "(require 'clojure.pprint '[clojure.edn :as edn] '[clojure [string :as str] [set :as set]])"
      (single-rule-config rule-name)))
  (it "works"
    (expect-match
      [{:rule-name rule-name
        :form '[clojure [string :as str] [set :as set]]
        :message "Don't use prefix libspecs in require calls"}]
      "(ns foo.bar (:require clojure.pprint [clojure.edn :as edn] [clojure [string :as str] [set :as set]]))"
      (single-rule-config rule-name))))
