; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.runner :as splint]
    [noahtheduke.splint.utils.test-runner :refer [with-out-str-data-map]]))

(defexpect dogfooding-test
  (expect
    (match? {:result {:diagnostics []
                      :exit 0}}
            (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
                (with-out-str-data-map)))))
