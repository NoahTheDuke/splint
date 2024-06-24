; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.require-explicit-param-tags-test
  (:require
   [lazytest.core :refer [defdescribe describe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config
  ([] (assoc (single-rule-config 'lint/require-explicit-param-tags)
        :clojure-version {:major 1 :minor 12}))
  ([style]
   (assoc-in (config) '[lint/require-explicit-param-tags :chosen-style] style)))

(defdescribe require-explicit-param-tags
  (describe "no tag"
    (describe "chosen style"
      (map (fn [style]
             (it style
               (expect-match
                 [{:rule-name 'lint/require-explicit-param-tags
                   :form 'File/mkdir
                   :message "Set explicit :param-tags on method values"
                   :alt nil}]
                 "(ns foo (:import (java.io File))) (File/mkdir (clojure.java.io/file \"a\"))"
                 (config style))))
           [:missing :both])
      (it :wildcard
        (expect-match
          nil
          "(ns foo (:import (java.io File))) (File/mkdir (clojure.java.io/file \"a\"))"
          (config :wildcard)))))

  (describe "with tag"
    (describe "chosen style"
      (map (fn [style]
             (it style
               (expect-match
                 [{:rule-name 'lint/require-explicit-param-tags
                   :form 'File/createTempFile
                   :message "Prefer explicit :param-tags on method values"
                   :alt nil}]
                 "(ns foo (:import (java.io File))) (^[_ _] File/createTempFile \"abc\" \"b\")"
                 (config style))))
           [:wildcard :both])
      (it :missing
        (expect-match
          nil
          "(ns foo (:import (java.io File))) (^[_ _] File/createTempFile \"abc\" \"b\")"
          (config :missing)))))

  (describe "Wrong version"
    (it "doesn't match"
      (expect-match
        nil
        "(^[_ _] File/createTempFile \"abc\" \"b\")"
        (assoc (config :both)
               :clojure-version {:major 1 :minor 11}))))

  (describe "ignores conforming param-tags"
    (map (fn [style]
           (it style
             (expect-match
               nil
               "(^[] String/toUpperCase \"hi\")"
               (config style))
             (expect-match
               nil
               "(ns foo (:import (java.util Date))) (^[long] Date/new 1707771694522)"
               (config style))))
         [:missing :wildcard :both])))
