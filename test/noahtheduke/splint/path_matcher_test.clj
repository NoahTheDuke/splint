; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.path-matcher-test
  (:require
   [babashka.fs :as fs]
   [lazytest.core :refer [defdescribe expect it]]
   [noahtheduke.splint.path-matcher :as sut]))

(set! *warn-on-reflection* true)

(defdescribe matches-test
  (it "glob:"
    (let [foo (fs/file "aa/bb/foo.clj")
          bar (fs/file "aa/bb/bar.clj")
          qux (fs/file "aa/cc/qux.clj")
          lup (fs/file "lup.clj")]
      (expect (sut/matches (sut/->matcher "glob:**/foo.clj") foo))
      (expect [true true true false]
        (mapv (partial sut/matches (sut/->matcher "glob:aa/**"))
          [foo bar qux lup]))
      (expect [true true false false]
        (mapv (partial sut/matches (sut/->matcher "glob:**/bb/*"))
          [foo bar qux lup]))))
  (it "regex:"
    (let [foo (fs/file "aa/bb/foo.clj")
          bar (fs/file "aa/bb/bar.clj")
          qux (fs/file "aa/cc/qux.clj")
          lup (fs/file "lup.clj")]
      (expect (sut/matches (sut/->matcher "regex:.*foo\\.clj") foo))
      (expect [true true true false]
        (mapv (partial sut/matches (sut/->matcher "regex:aa/.*/.*\\.clj"))
          [foo bar qux lup]))
      (expect [false false false true]
        (mapv (partial sut/matches (sut/->matcher "regex:lup\\.clj"))
          [foo bar qux lup]))))
  (it "re-find:"
    (let [foo (fs/file "aa/bb/foo.clj")
          bar (fs/file "aa/bb/bar.clj")
          qux (fs/file "aa/cc/qux.clj")
          lup (fs/file "lup.clj")]
      (expect (sut/matches (sut/->matcher "re-find:foo.clj") foo))
      (expect [true true true false]
        (mapv (partial sut/matches (sut/->matcher "re-find:aa/.*/"))
          [foo bar qux lup]))
      (expect [false false false true]
        (mapv (partial sut/matches (sut/->matcher "re-find:lup.clj"))
          [foo bar qux lup]))))
  (it "string:"
    (let [foo (fs/file "aa/bb/foo.clj")
          bar (fs/file "aa/bb/bar.clj")
          qux (fs/file "aa/cc/qux.clj")
          lup (fs/file "lup.clj")]
      (expect (sut/matches (sut/->matcher "string:foo.clj") foo))
      (expect [false false false false]
        (mapv (partial sut/matches (sut/->matcher "string:aa/.*/"))
          [foo bar qux lup]))
      (expect [true true false false]
        (mapv (partial sut/matches (sut/->matcher "string:b"))
          [foo bar qux lup])))))
