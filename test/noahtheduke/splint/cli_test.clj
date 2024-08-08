; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.cli-test
  (:require
   [lazytest.core :refer [defdescribe it expect]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :refer [absent]]
   [noahtheduke.splint.cli :as sut]))

(set! *warn-on-reflection* true)

(defdescribe validate-opts-test
  (it "--help"
    (expect
      (match? {:exit-message #"splint v.*\n\nUsage"
               :ok true}
              (sut/validate-opts ["-h"])))
    (expect
      (match? {:exit-message absent
               :ok absent}
              (sut/validate-opts []))))
  (it "--version"
    (expect (re-find #"splint v\d" (:exit-message (sut/validate-opts ["-v"]))))
    (expect (re-find #"splint v\d" (:exit-message (sut/validate-opts ["--version"])))))
  (it "errors"
    (expect
      (match? {:exit-message #"splint errors:\nUnknown option:"
               :ok false}
              (sut/validate-opts ["-asdf"])))
    (expect
      (match? {:exit-message "splint errors:\n\"--quiet\" must come before paths"
               :ok false}
              (sut/validate-opts ["--" "--quiet"]))))
  (it "--summary"
    (expect
      (match? {:options {:summary true}}
              (sut/validate-opts ["--summary" "files"])))
    (expect
      (match? {:options {:summary false}}
              (sut/validate-opts ["--no-summary" "files"]))))
  (it "--quiet"
    (expect
      (match? {:options {:quiet absent}}
              (sut/validate-opts ["files"])))
    (expect
      (match? {:options {:quiet true}}
              (sut/validate-opts ["-q" "files"])))
    (expect
      (match? {:options {:quiet true}}
              (sut/validate-opts ["--quiet" "files"]))))
  (it "--silent"
    (expect
      (match? {:options {:silent absent}}
              (sut/validate-opts ["files"])))
    (expect
      (match? {:options {:silent true}}
              (sut/validate-opts ["-s" "files"])))
    (expect
      (match? {:options {:silent true}}
              (sut/validate-opts ["--silent" "files"]))))
  (it "--print-config"
    (expect
      (match? {:exit-message #"Diff:"
               :ok true}
              (sut/validate-opts ["--print-config" "diff"])))
    (expect
      (match? {:exit-message #"Local:"
               :ok true}
              (sut/validate-opts ["--print-config" "local"])))
    (expect
      (match? {:exit-message #"Full:"
               :ok true}
              (sut/validate-opts ["--print-config" "full"]))))
  (it "-- paths"
    (expect
      (match? {:paths ["files"]}
              (sut/validate-opts ["files"])))
    (expect
      (match? {:paths ["a" "b" "c"]}
              (sut/validate-opts ["a" "b" "c"])))
    (expect
      (match? {:paths ["files"]}
              (sut/validate-opts ["--" "files"]))))
  (it "--require"
    (expect
      (match? {:options {:required-files ["a"]}}
              (sut/validate-opts ["-r" "a"])))
    (expect
      (match? {:options {:required-files ["a"]}}
              (sut/validate-opts ["--require" "a"])))
    (expect
      (match? {:options {:required-files ["a" "b"]}}
              (sut/validate-opts ["--require" "a" "--require" "b"]))))
  (it "--only"
    (expect
      (match? {:options {:only #{'style/def-fn}}}
              (sut/validate-opts ["--only" "style/def-fn"])))
    (expect
      (match? {:options {:only #{'style/def-fn}}}
              (sut/validate-opts ["--only" "style/def-fn" "--only" "style/def-fn"])))
    (expect
      (match? {:options {:only #{'style/def-fn 'performance}}}
              (sut/validate-opts ["--only" "style/def-fn" "--only" "performance"])))))
