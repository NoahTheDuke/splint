; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser.ns-test
  (:require
    [lazytest.core :refer [defdescribe expect it]]
    [noahtheduke.splint.parser.ns :as sut]))

(set! *warn-on-reflection* true)

(defdescribe parse-ns-test
  (it "respects import"
    (expect
      (= '{:imports
           {a a
            c b.c
            b.c b.c
            i g.h.i
            g.h.i g.h.i
            j g.h.j
            g.h.j g.h.j
            k g.h.k
            g.h.k g.h.k}}
         (sut/parse-ns
           '(import a b.c (d.e.f) (g.h i j k))))))

  (it "respects require"
    (expect
      (= '{:aliases {str clojure.string
                     react-dom "react-dom"}}
         (sut/parse-ns
           '(require
              [clojure.string :refer [join] :as-alias str]
              ["react-dom" :refer [cool-stuff] :as react-dom]
              :reload-all)))))

  (it "respects :as and :as-alias"
    (expect
      (= '{:aliases {set clojure.set}}
         (sut/parse-ns
           '(use [clojure.set :as set]))))
    (expect
      (= '{:aliases {set clojure.set}}
         (sut/parse-ns
           '(use [clojure.set :as-alias set])))))

  (it "handles ns form"
    (expect
      (= '{:current noahtheduke.splint.ns-parser
           :aliases {str clojure.string
                     react-dom "react-dom"
                     z clojure.zip
                     set clojure.set
                     edn clojure.edn}
           :imports {Byte java.lang.Byte
                     java.lang.Byte java.lang.Byte
                     Character java.lang.Character
                     java.lang.Character java.lang.Character
                     ArrayList java.lang.ArrayList
                     java.lang.ArrayList java.lang.ArrayList}}
         (sut/parse-ns
           '(ns noahtheduke.splint.ns-parser
              (:refer-clojure :exclude [asdf])
              (:use [clojure.set :as-alias set])
              (:use [clojure.edn :as edn])
              (:require
                [clojure.string :refer [join] :as-alias str]
                ["react-dom" :refer [cool-stuff] :as react-dom]
                :reload-all)
              (:require
                [clojure.zip :reload :all :as z])
              (:import
                java.lang.Byte
                (java.lang Character ArrayList)))))))

  (it "respects in-ns"
    (expect
      (= '{:current foo}
         (sut/parse-ns '(in-ns 'foo)))))

  (it "respects alias"
    (expect (= '{:aliases {asdf qwer.qwer}}
               (sut/parse-ns '(alias 'asdf 'qwer.qwer))))
    (doseq [input ['(alias a b)
                   '(alias 'a b)
                   '(alias a 'b)]]
      (expect (nil? (sut/parse-ns input)))))

  (it "ignores refer"
    (expect (nil?
              (sut/parse-ns
                '(refer clojure.string :only [join]))))
    (expect (nil?
              (sut/parse-ns
                '(refer clojure.string :only [join]))))))
