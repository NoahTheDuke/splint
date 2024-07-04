; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config-test
  (:require
   [lazytest.core :refer [defdescribe it expect describe given]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.config :as sut]
   [noahtheduke.splint.path-matcher :refer [->matcher]]
   [noahtheduke.splint.test-helpers :refer [print-to-file! with-temp-files]]))

(set! *warn-on-reflection* true)

(defdescribe load-config-test
  (it "sets the defaults"
    (expect (match? {:parallel true :output "full"}
              (sut/load-config nil)))
    (expect (match? {:output "simple"}
              (sut/load-config {'output "clj-kondo"} {:output "simple"})))
    (expect (match? {:parallel false}
              (sut/load-config {'parallel false} nil)))
    (expect (match? {:output "simple"}
              (sut/load-config {'output "simple"} nil))))

  (describe ":required-files"
    (it "merges required-files correctly"
      (with-redefs [sut/require-file! identity]
        (expect (match? {:required-files nil}
                        (sut/load-config {} nil)))
        (expect (match? {:required-files ["a" "b"]}
                        (sut/load-config {:require ["a" "b"]} nil)))
        (expect (match? {:required-files ["a" "b" "c" "d"]}
                        (sut/load-config {:require ["a" "b"]} {:required-files ["c" "d"]})))))))

(defdescribe disable-single-rule-test
  (it "handles rules"
    (expect (match? {'style/plus-one {:enabled false}}
              (sut/load-config {'style/plus-one {:enabled false}} nil)))))

(defdescribe disable-genre-test
  (describe ":enable"
    (given [config (update-vals @sut/default-config #(assoc % :enabled true))]
      (it "can disable a whole genre"
        (doseq [c (->> (sut/merge-config config {'style {:enabled false}})
                    (vals)
                    (filter #(.equals "style" (namespace (:rule-name % :a)))))]
          (expect (match? {:enabled false} c))))
      (it "chooses the more specific config"
        (expect
          (match? {'style/plus-one {:enabled true}}
            (sut/merge-config config {'style {:enabled false}
                                      'style/plus-one {:enabled true}})))))))

(defn get-global [a]
  (select-keys a [:global]))

(defdescribe global-config-test
  (it "merged global excludes correctly"
    (expect (match? {:global {:excludes [(->matcher "foo")]}}
              (get-global (sut/load-config {'global {:excludes ["foo"]}} nil)))
      "a good mach")
    (expect (match? {:global {:excludes [(->matcher "glob:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["glob:foo"]}} nil))))
    (expect (match? {:global {:excludes [(->matcher "regex:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["regex:foo"]}} nil))))
    (expect (match? {:global {:excludes [(->matcher "re-find:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["re-find:foo"]}} nil))))
    (expect (match? {:global {:excludes [(->matcher "string:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["string:foo"]}} nil)))))
  (it "defaults to re-find"
    (expect (match? {:global {:excludes [(->matcher "re-find:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["foo"]}} nil)))))
  (it "can handle multiple"
    (expect (match? {:global {:excludes [(->matcher "foo")
                                         (->matcher "regex:foo")]}}
              (get-global (sut/load-config {'global {:excludes ["foo" "regex:foo"]}} nil))))))

(defdescribe read-project-file
  (describe "deps.edn"
    (with-temp-files [deps-edn "deps.edn"]
      (print-to-file!
        deps-edn
        "{:deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}}")
      (expect
        (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                   :qualifier nil :snapshot nil}
                 :paths []}
          (sut/read-project-file deps-edn nil))))
    (it "with multiple dependencies"
      (with-temp-files [deps-edn "deps.edn"]
        (print-to-file!
          deps-edn
          "{:deps {org.clojure/clojure {:mvn/version \"1.8.0\"}
                 org.clojure/core.async {:mvn/version \"1.5.644\"}}}")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                     :qualifier nil :snapshot nil}
                   :paths []}
            (sut/read-project-file deps-edn nil)))))
    (it "with paths"
      (with-temp-files [deps-edn "deps.edn"]
        (print-to-file!
          deps-edn
          "{:paths [\"src\" \"test\"]
          :deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}}")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0}
                   :paths ["src" "test"]}
            (sut/read-project-file deps-edn nil)))))
    (it "with paths in aliases"
      (with-temp-files [deps-edn "deps.edn"]
        (print-to-file!
          deps-edn
          "{:paths [\"src\"]
          :deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}
          :aliases {:dev {:extra-paths [\"dev\"]}
                    :test {:extra-paths [\"test\"]}}}")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0}
                   :paths ["src" "dev" "test"]}
            (sut/read-project-file deps-edn nil))))))

  (describe "project.clj"
    (it "can be read"
      (with-temp-files [project-clj "project.clj"]
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
             :dependencies [[org.clojure/clojure \"1.8.0\"]])")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                     :qualifier nil :snapshot nil}
                   :paths ["src" "test"]}
            (sut/read-project-file nil project-clj)))))
    (it "dependencies with exclusions"
      (with-temp-files [project-clj "project.clj"]
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
             :dependencies [[org.clojure/clojure \"1.8.0\" :exclusions []]])")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0}}
            (sut/read-project-file nil project-clj)))))
    (it "multiple dependencies"
      (with-temp-files [project-clj "project.clj"]
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
             :dependencies [[org.clojure/core.async \"1.5.644\"]
                            [org.clojure/clojure \"1.8.0\"]])")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0}}
            (sut/read-project-file nil project-clj)))))
    (it "paths"
      (with-temp-files [project-clj "project.clj"]
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
             :dependencies [[org.clojure/clojure \"1.8.0\"]]
             :source-paths [\"source\"]
             :test-paths [\"testing\"])")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                     :qualifier nil :snapshot nil}
                   :paths ["source" "testing"]}
            (sut/read-project-file nil project-clj)))))
    (it "paths with alises"
      (with-temp-files [project-clj "project.clj"]
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
             :dependencies [[org.clojure/clojure \"1.8.0\"]]
             :source-paths [\"source\"]
             :test-paths [\"testing\"]
             :profiles {:dev {:source-paths [\"dev-source\"]}
                        :test {:test-paths [\"test-source\"]}})")
        (expect
          (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                     :qualifier nil :snapshot nil}
                   :paths ["source" "testing"
                           "dev-source" "test-source"]}
            (sut/read-project-file nil project-clj))))))

  (describe "both deps.edn and project.clj"
    (it "prefers deps.edn"
      (with-temp-files [deps-edn "deps.edn"
                        project-clj "project.clj"]
        (print-to-file! deps-edn "{:deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}}")
        (print-to-file!
          project-clj
          "(defproject foo \"1\"
         :dependencies [[org.clojure/clojure \"1.10.0\"]])")
        (expect
          (match? {:clojure-version {:major 1
                                     :minor 8
                                     :incremental 0
                                     :qualifier nil
                                     :snapshot nil}
                   :paths []}
            (sut/read-project-file deps-edn project-clj)))))))

(defdescribe parse-project-file-features-test
  (it "correctly reads project.clj with fancy symbols"
    (with-temp-files [project-clj "project.clj"]
      (print-to-file!
        project-clj
        "(defproject foo nil
           :dependencies [[org.clojure/clojure \"1.10.0\"]]
           :unquote ~foo
           :unquote-splicing [~@(map inc (range 100))]
           :read-eval #=(+ 1 2)
           :regex #\".*\"
           :fn #(+ % %)
           :var #'abc)")
      (expect
        (sut/read-project-file nil project-clj)))))
