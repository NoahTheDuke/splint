; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.config-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each in expecting]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.config :as sut]
    [noahtheduke.splint.test-helpers :refer [with-temp-files print-to-file!]]))

(defexpect load-config-test
  (expect (match? {:parallel true :output "full"}
                  (sut/load-config nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "clj-kondo"}
                                   {:output "simple"})))
  (expect (match? {:parallel false}
                  (sut/load-config {'parallel false} nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "simple"} nil))))

(defexpect disable-single-rule-test
  (expect (match? {'style/plus-one {:enabled false}}
                  (sut/load-config {'style/plus-one {:enabled false}}
                                   nil))))

(defexpect disable-genre-test
  (let [config (update-vals @sut/default-config #(assoc % :enabled true))]
    (expect
      {:enabled false}
      (from-each [c (->> (sut/merge-config config {'style {:enabled false}})
                         (vals)
                         (filter #(= "style" (namespace (:rule-name % :a)))))]
        (in c)))
    (expect
      (match? {'style/plus-one {:enabled true}}
              (sut/merge-config config {'style {:enabled false}
                                        'style/plus-one {:enabled true}})))))

(defexpect read-project-file-edn-test
  (with-temp-files [deps-edn "deps.edn"]
    (print-to-file!
      deps-edn
      "{:deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}}")
    (expect
      (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                 :qualifier nil :snapshot nil}
               :paths []}
              (sut/read-project-file deps-edn nil))))
  (expecting "with multiple dependencies"
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
  (expecting "with paths"
    (with-temp-files [deps-edn "deps.edn"]
      (print-to-file!
        deps-edn
        "{:paths [\"src\" \"test\"]
          :deps {org.clojure/clojure {:mvn/version \"1.8.0\"}}}")
      (expect
        (match? {:clojure-version {:major 1 :minor 8 :incremental 0}
                 :paths ["src" "test"]}
                (sut/read-project-file deps-edn nil)))))
  (expecting "with paths in aliases"
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

(defexpect read-project-file-project-clj-test
  (with-temp-files [project-clj "project.clj"]
    (print-to-file!
      project-clj
      "(defproject foo \"1\"
         :dependencies [[org.clojure/clojure \"1.8.0\"]])")
    (expect
      (match? {:clojure-version {:major 1 :minor 8 :incremental 0
                                 :qualifier nil :snapshot nil}
               :paths ["src" "test"]}
              (sut/read-project-file nil project-clj))))
  (expecting "dependencies with exclusions"
    (with-temp-files [project-clj "project.clj"]
      (print-to-file!
        project-clj
        "(defproject foo \"1\"
           :dependencies [[org.clojure/clojure \"1.8.0\" :exclusions []]])")
      (expect
        (match? {:clojure-version {:major 1 :minor 8 :incremental 0}}
                (sut/read-project-file nil project-clj)))))
  (expecting "multiple dependencies"
    (with-temp-files [project-clj "project.clj"]
      (print-to-file!
        project-clj
        "(defproject foo \"1\"
           :dependencies [[org.clojure/core.async \"1.5.644\"]
                          [org.clojure/clojure \"1.8.0\"]])")
      (expect
        (match? {:clojure-version {:major 1 :minor 8 :incremental 0}}
                (sut/read-project-file nil project-clj)))))
  (expecting "paths"
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
  (expecting "paths with alises"
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

(defexpect read-project-file-both-test
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
              (sut/read-project-file deps-edn project-clj)))))
