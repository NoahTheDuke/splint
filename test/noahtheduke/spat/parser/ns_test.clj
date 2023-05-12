(ns noahtheduke.spat.parser.ns-test
  (:require [expectations.clojure.test :refer [defexpect expect from-each]]
            [noahtheduke.spat.parser.ns :as sut]))

(defexpect parse-ns-import-test
  (expect
    '{:imports
      {a a,
       b.c b.c,
       i g.h.i,
       g.h.i g.h.i,
       j g.h.j,
       g.h.j g.h.j,
       k g.h.k,
       g.h.k g.h.k}}
    (sut/parse-ns
      '(import a b.c (d.e.f) (g.h i j k)))))

(defexpect parse-ns-require-test
  (expect
    '{:aliases {str clojure.string
                react-dom "react-dom"}}
    (sut/parse-ns
      '(require
         [clojure.string :refer [join] :as-alias str]
         ["react-dom" :refer [cool-stuff] :as react-dom]
         :reload-all))))

(defexpect parse-ns-use-test
  (expect
    '{:aliases {set clojure.set}}
    (sut/parse-ns
      '(use [clojure.set :as set])))
  (expect
    '{:aliases {set clojure.set}}
    (sut/parse-ns
      '(use [clojure.set :as-alias set]))))

(defexpect parse-ns-ns-test
  (expect
    '{:current noahtheduke.spat.ns-parser,
      :aliases
      {str clojure.string,
       react-dom "react-dom",
       z clojure.zip,
       set clojure.set,
       edn clojure.edn},
      :imports
      {java.lang.Byte java.lang.Byte,
       Character java.lang.Character,
       java.lang.Character java.lang.Character,
       ArrayList java.lang.ArrayList,
       java.lang.ArrayList java.lang.ArrayList}}
    (sut/parse-ns
      '(ns noahtheduke.spat.ns-parser
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
           (java.lang Character ArrayList))))))

(defexpect parse-ns-in-ns-test
  (expect
    '{:current foo}
    (sut/parse-ns '(in-ns 'foo))))

(defexpect parse-ns-alias-test
  (expect '{:aliases {asdf qwer.qwer}}
    (sut/parse-ns '(alias 'asdf 'qwer.qwer)))
  (expect nil?
    (from-each [input ['(alias a b)
                       '(alias 'a b)
                       '(alias a 'b)]]
      (sut/parse-ns input))))

(defexpect parse-ns-refer-test
  (expect nil?
    (sut/parse-ns
      '(refer clojure.string :only [join]))))

(defexpect parse-ns-refer-clojure-test
  (expect nil?
    (sut/parse-ns
      '(refer clojure.string :only [join]))))
