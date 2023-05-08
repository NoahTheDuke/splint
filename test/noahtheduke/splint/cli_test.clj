; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli-test 
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.matchers :refer [absent]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.cli :as sut]))

(defexpect help-message-test
  (expect
    (match? {:exit-message #"splint v.*\n\nUsage"
             :ok true}
            (sut/validate-opts ["-h"])))
  (expect
    (match? {:exit-message #"splint v.*\n\nUsage"
             :ok true}
            (sut/validate-opts []))))

(defexpect version-test
  (expect #"splint v\d" (:exit-message (sut/validate-opts ["-v"])))
  (expect #"splint v\d" (:exit-message (sut/validate-opts ["--version"]))))

(defexpect errors-test
  (expect
    (match? {:exit-message #"splint errors:\nUnknown option:"
             :ok false}
            (sut/validate-opts ["-asdf"])))
  (expect
    (match? {:exit-message "splint errors:\n\"--quiet\" must come before paths"
             :ok false}
            (sut/validate-opts ["--" "--quiet"]))))

(defexpect quiet-test
  (expect
    (match? {:options {:quiet absent}}
            (sut/validate-opts ["files"])))
  (expect
    (match? {:options {:quiet true}}
            (sut/validate-opts ["-q" "files"])))
  (expect
    (match? {:options {:quiet true}}
            (sut/validate-opts ["--quiet" "files"]))))

(defexpect print-config-test
  (expect
    (match? {:exit-message #"Diff:"
             :ok true}
            (sut/validate-opts ["--config" "diff"])))
  (expect
    (match? {:exit-message #"Local:"
             :ok true}
            (sut/validate-opts ["--config" "local"])))
  (expect
    (match? {:exit-message #"Full:"
             :ok true}
            (sut/validate-opts ["--config" "full"]))))

(defexpect paths-test
  (expect
    (match? {:paths ["files"]}
            (sut/validate-opts ["files"])))
  (expect
    (match? {:paths ["a" "b" "c"]}
            (sut/validate-opts ["a" "b" "c"])))
  (expect
    (match? {:paths ["files"]}
            (sut/validate-opts ["--" "files"]))))
