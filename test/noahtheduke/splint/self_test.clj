; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test]
    [noahtheduke.splint.runner :as splint]
    [noahtheduke.splint.test-helpers :refer [with-out-str-data-map]]))

(defexpect dogfooding-test
  (expect
    (match? {:result {:diagnostics []
                      :exit 0}}
            (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
                (with-out-str-data-map)))))
