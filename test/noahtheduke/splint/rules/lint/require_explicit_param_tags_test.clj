; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.require-explicit-param-tags-test
  (:require
   [clojure.test :refer [deftest]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config
  ([] (assoc (single-rule-config 'lint/require-explicit-param-tags)
        :clojure-version {:major 1 :minor 12}))
  ([style]
   (assoc-in (config) '[lint/require-explicit-param-tags :chosen-style] style)))

(deftest missing-test
  (doseq [style [:missing :both]]
    (expect-match
      [{:rule-name 'lint/require-explicit-param-tags
        :form 'File/mkdir
        :message "Set explicit :param-tags on method values"
        :alt nil}]
      "(ns foo (:import (java.io File))) (File/mkdir (clojure.java.io/file \"a\"))"
      (config style)))
  (expect-match
    nil
    "(ns foo (:import (java.io File))) (File/mkdir (clojure.java.io/file \"a\"))"
    (config :wildcard)))

(deftest wildcard-test
  (doseq [style [:wildcard :both]]
    (expect-match
      [{:rule-name 'lint/require-explicit-param-tags
        :form 'File/createTempFile
        :message "Prefer explicit :param-tags on method values"
        :alt nil}]
      "(ns foo (:import (java.io File))) (^[_ _] File/createTempFile \"abc\" \"b\")"
      (config style)))
  (expect-match
    nil
    "(ns foo (:import (java.io File))) (^[_ _] File/createTempFile \"abc\" \"b\")"
    (config :missing)))

(deftest under-version-test
  (expect-match
    nil
    "(^[_ _] File/createTempFile \"abc\" \"b\")"
    (assoc (config :both)
      :clojure-version {:major 1 :minor 11})))

(deftest passing-test
  (doseq [style [:missing :wildcard :both]]
    (expect-match
      nil
      "(^[] String/toUpperCase \"hi\")"
      (config style))
    (expect-match
      nil
      "(ns foo (:import (java.util Date))) (^[long] Date/new 1707771694522)"
      (config style))))
