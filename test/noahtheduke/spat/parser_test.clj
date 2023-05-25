; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.matchers :refer [absent]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.spat.parser :as parser :refer [parse-string parse-string-all]]))

(defexpect unknown-tagged-literals-test
  '[(splint/tagged-literal (sql/raw [1 2 3]))
    (splint/tagged-literal (unknown [4]))]
  (parse-string-all "#sql/raw [1 2 3] #unknown [4]"))

(defexpect auto-resolve-kw-test
  (expect '[:splint-auto-current_/foo :splint-auto-alias_foo/bar :foo :foo/bar]
    (parse-string-all "::foo ::foo/bar :foo :foo/bar"))
  (expect '[(ns foo (:require [clojure.set :as set])) :clojure.set/foo]
    (parse-string-all "(ns foo (:require [clojure.set :as set])) ::set/foo")))

(defexpect discard-metadata-test
  (expect
    (match? {:splint/disable true}
            (meta (parse-string "#_:splint/disable (foo bar)"))))
  (expect
    (match? '{:splint/disable [lint]}
            (meta (parse-string "#_{:splint/disable [lint]} (foo bar)")))))

(defexpect defn-symbol-test
  (expect '(defn example "docstring" [] (+ 1 1))
    (parse-string "(defn example \"docstring\" [] (+ 1 1))"))
  (expect
    (match? '{:spat/defn-form
              {:spat/name example
               :doc "docstring"
               :arities (([] (+ 1 1)))
               :arglists ([])}}
            (meta (parse-string "(defn example \"docstring\" [] (+ 1 1))"))))
  (expect
    (match? {:spat/defn-form absent}
            (meta (parse-string "(defn-partial abc (+ 1 2 3))")))))
