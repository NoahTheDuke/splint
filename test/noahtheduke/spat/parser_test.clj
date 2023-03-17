; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.spat.parser :refer [parse-string parse-string-all]]))

(defexpect unknown-tagged-literals-test
  '[(sql/raw [1 2 3]) (splint-auto/unknown [4])]
  (parse-string-all "#sql/raw [1 2 3] #unknown [4]"))

(defexpect auto-resolve-kw-test
  (expect '[:splint-auto_/foo :splint-auto_foo/bar :foo :foo/bar]
    (parse-string-all "::foo ::foo/bar :foo :foo/bar"))
  (expect '[(ns foo (:require [clojure.set :as set])) :clojure.set/foo]
    (parse-string-all "(ns foo (:require [clojure.set :as set])) ::set/foo")))

(defexpect discard-metadata-test
  (expect {:splint/disable true}
    (select-keys (meta (parse-string "#_:splint/disable (foo bar)"))
                 [:splint/disable]))
  (expect '{:splint/disable lint}
    (select-keys (meta (parse-string "#_{:splint/disable lint} (foo bar)"))
                 [:splint/disable])))
