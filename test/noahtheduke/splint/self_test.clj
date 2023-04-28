; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.self-test
  (:require
    [expectations.clojure.test :refer [defexpect expect in]]
    [noahtheduke.splint.runner :as splint]))

(defmacro with-out-str-data-map
  [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [*out* s#]
       (let [r# (do ~@body)]
         {:result r#
          :str (str s#)}))))

(defexpect dogfooding-test
  (expect {:diagnostics []
           :exit 0}
    (in (-> (splint/run ["--quiet" "--no-parallel" "dev" "src" "test"])
            (with-out-str-data-map)
            (:result)))))
