; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser-test
  (:require
    [expectations.clojure.test :refer [defexpect expect from-each]]
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
  (expect {:splint/disable true}
    (select-keys (meta (parse-string "#_:splint/disable (foo bar)"))
                 [:splint/disable]))
  (expect '{:splint/disable [lint]}
    (select-keys (meta (parse-string "#_{:splint/disable [lint]} (foo bar)"))
                 [:splint/disable])))

(defexpect parse-ns-test
  (let [parse-ns @#'parser/parse-ns]
    (expect
      '{:current noahtheduke.spat.ns-parser
        :aliases {str clojure.string
                  react-dom "react-dom"
                  z clojure.zip
                  set clojure.set
                  edn clojure.edn}}
      (parse-ns
        '(ns noahtheduke.spat.ns-parser
           (:use [clojure.set :as-alias set])
           (:use [clojure.edn :as edn])
           (:require
             [clojure.string :refer [join] :as-alias str]
             ["react-dom" :refer [cool-stuff] :as react-dom]
             :reload-all)
           (:require
             [clojure.zip :reload :all :as z]))))
    (expect
      '{:aliases {str clojure.string
                  react-dom "react-dom"}}
      (parse-ns
        '(require
           [clojure.string :refer [join] :as-alias str]
           ["react-dom" :refer [cool-stuff] :as react-dom]
           :reload-all)))
    (expect
      '{:aliases {str clojure.string
                  react-dom "react-dom"}}
      (parse-ns
        '(use
           [clojure.string :refer [join] :as-alias str]
           ["react-dom" :refer [cool-stuff] :as react-dom]
           :reload-all)))
    (expect
      '{:current noahtheduke.spat.parser}
      (parse-ns '(in-ns 'noahtheduke.spat.parser)))
    (expect
      '{:aliases {asdf qwer.qwer}}
      (parse-ns '(alias 'asdf 'qwer.qwer)))
    (expect '{:aliases nil}
      (from-each [input ['(alias a b)
                         '(alias 'a b)
                         '(alias a 'b)]]
        (parse-ns input)))))
