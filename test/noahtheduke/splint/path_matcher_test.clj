; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher-test
  (:require
    [clojure.java.io :as io]
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.path-matcher :as sut]))

(set! *warn-on-reflection* true)

(defexpect matcher-glob-test
  (let [foo (io/file "aa/bb/foo.clj")
        bar (io/file "aa/bb/bar.clj")
        qux (io/file "aa/cc/qux.clj")
        lup (io/file "lup.clj")]
    (expect (sut/matches (sut/->matcher "glob:**/foo.clj") foo))
    (expect [true true true false]
      (mapv (partial sut/matches (sut/->matcher "glob:aa/**"))
            [foo bar qux lup]))
    (expect [true true false false]
      (mapv (partial sut/matches (sut/->matcher "glob:**/bb/*"))
            [foo bar qux lup]))))

(defexpect matcher-regex-test
  (let [foo (io/file "aa/bb/foo.clj")
        bar (io/file "aa/bb/bar.clj")
        qux (io/file "aa/cc/qux.clj")
        lup (io/file "lup.clj")]
    (expect (sut/matches (sut/->matcher "regex:.*foo\\.clj") foo))
    (expect [true true true false]
          (mapv (partial sut/matches (sut/->matcher "regex:aa/.*/.*\\.clj"))
                [foo bar qux lup]))
    (expect [false false false true]
      (mapv (partial sut/matches (sut/->matcher "regex:lup\\.clj"))
            [foo bar qux lup]))))

(defexpect matcher-re-find-test
  (let [foo (io/file "aa/bb/foo.clj")
        bar (io/file "aa/bb/bar.clj")
        qux (io/file "aa/cc/qux.clj")
        lup (io/file "lup.clj")]
    (expect (sut/matches (sut/->matcher "re-find:foo.clj") foo))
    (expect [true true true false]
          (mapv (partial sut/matches (sut/->matcher "re-find:aa/.*/"))
                [foo bar qux lup]))
    (expect [false false false true]
      (mapv (partial sut/matches (sut/->matcher "re-find:lup.clj"))
            [foo bar qux lup]))))

(defexpect matcher-string-test
  (let [foo (io/file "aa/bb/foo.clj")
        bar (io/file "aa/bb/bar.clj")
        qux (io/file "aa/cc/qux.clj")
        lup (io/file "lup.clj")]
    (expect (sut/matches (sut/->matcher "string:foo.clj") foo))
    (expect [false false false false]
          (mapv (partial sut/matches (sut/->matcher "string:aa/.*/"))
                [foo bar qux lup]))
    (expect [true true false false]
      (mapv (partial sut/matches (sut/->matcher "string:b"))
            [foo bar qux lup]))))
