; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.new-object-test
  (:require
   [lazytest.core :refer [defdescribe it describe]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(def rule-name 'style/new-object)

(defn config
  ([] (config nil))
  ([style]
   (cond-> (single-rule-config rule-name)
     true (assoc :clojure-version {:major 1 :minor 11})
     style (update rule-name merge style))))

(defn method-config []
  (-> (config {:chosen-style :method-value})
      (assoc :clojure-version {:major 1 :minor 12})))

(defdescribe new-object-test
  (describe ":chosen-style"
    (it ":dot"
      (expect-match
        [{:rule-name rule-name
          :form '(new java.util.ArrayList 100)
          :alt '(java.util.ArrayList. 100)
          :message "Foo. is preferred."}]
        "(new java.util.ArrayList 100)"
        (single-rule-config rule-name)))
    (describe ":method-value"
      (it "works"
        (expect-match
          [{:rule-name rule-name
            :form '(new java.util.ArrayList 100)
            :alt '(java.util.ArrayList/new 100)
            :message "Foo/new is preferred."}]
          "(new java.util.ArrayList 100)"
          (method-config)))
      (it "ignores chosen style in clojure 1.11"
        (expect-match
          [{:rule-name rule-name
            :form '(new java.util.ArrayList 100)
            :alt '(java.util.ArrayList. 100)
            :message "Foo. is preferred."}]
          "(new java.util.ArrayList 100)"
          (-> (method-config)
              (assoc :clojure-version {:major 1 :minor 11}))))
      (it "converts dot syntax"
        (expect-match
          [{:rule-name rule-name
            :form '(java.util.ArrayList. 100)
            :alt '(java.util.ArrayList/new 100)
            :message "Foo/new is preferred."}]
          "(java.util.ArrayList. 100)"
          (method-config)))))
  (it "ignores existing dots"
    (expect-match
      nil
      "(java.util.ArrayList. 100)"
      (single-rule-config rule-name))))
