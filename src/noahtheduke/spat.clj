; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat
  (:require
   [clj-kondo.impl.rewrite-clj.parser :as p]
   [methodical.core :as m]
   #_[hyperfiddle.rcf :as rcf]))

(set! *warn-on-reflection* true)

(def example-str
  "(+ 1 1 {1 2 3})")

(def example-input
  (p/parse-string example-str))

(def example-sexp
  '(+ 1 _ (:or 'int?)))

(defn read-dispatch [sexp]
  (cond
   (nil? sexp) :nil
   (boolean? sexp) :boolean
   (keyword? sexp) :keyword
   (list? sexp) :list
   (map? sexp) :map
   (number? sexp) :number
   (seq? sexp) :list
   (set? sexp) :set
   (string? sexp) :string
   (symbol? sexp) :symbol
   (vector? sexp) :vector
   :else (class sexp)))

(read-dispatch [(range 10)])

(m/defmulti read-form read-dispatch)

(m/defmethod read-form :default :before [sexp]
  (prn sexp))

(m/defmethod read-form :default [sexp]
  (prn (read-dispatch sexp)))

(m/defmethod read-form :list [sexp]
  (prn (count sexp)))

(comment
  (read-form example-input)
  (user/refresh-all)
  ,)
