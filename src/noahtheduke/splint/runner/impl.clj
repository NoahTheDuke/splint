; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.runner.impl
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.parser.ns :refer [deps-from-libspec]]
   [noahtheduke.splint.pattern :refer [pattern]]
   [noahtheduke.splint.pipeline :refer [queue]]
   [noahtheduke.splint.utils :refer [drop-quote]]))

(set! *warn-on-reflection* true)

(defn find-file [files stem]
  (reduce
    (fn [_ file]
      (when (re-find stem (str file))
        (reduced (str file))))
    nil
    files))

(defn load-dispatch [_ctx ?ns _?args] ?ns)

(defmulti load-impl #'load-dispatch :default 'ns)

(defmethod load-impl 'require
  [ctx _ ?args]
  (let [file-stems (->> ?args
                     (mapcat #(deps-from-libspec nil (drop-quote %)))
                     (keep :ns)
                     (distinct)
                     (map #(-> (name %)
                             (str/replace \- \.)
                             (str ".clj.?$")
                             (re-pattern))))
        {:keys [pending-files pipeline]} ctx
        filenames (keys pending-files)
        matched-files (into []
                        (comp
                          (keep #(find-file filenames %))
                          (map pending-files))
                        file-stems)
        pipeline (apply queue pipeline matched-files)
        pending-files (apply dissoc pending-files matched-files)]
    (assoc ctx
      :pending-files pending-files
      :pipeline pipeline)))

(defmethod load-impl 'use
  [ctx _ ?args]
  ctx)

(defmethod load-impl 'in-ns
  [ctx _ ?args]
  ctx)

(defmethod load-impl 'refer
  [ctx _ ?args]
  ctx)

(defmethod load-impl 'ns
  [ctx _ ?args]
  (->> ?args
    (filter sequential?)
    (remove (comp #{:gen-class :refer-clojure} first))
    (reduce (fn [ctx libspec]
              (load-impl ctx
                (symbol (name (first libspec)))
                (next libspec)))
      ctx)))

(defn loader-macro? [form]
  ('#{import in-ns ns require use} form))

(def load-pattern
  (pattern '((? ns loader-macro?) ?*args)))

(defn enqueue-loads
  "Check if a given form is a loading form (require, use, etc),
  and then enqueue all of the mentioned libs."
  [ctx form]
  (if-let [{:syms [?ns ?args]} (load-pattern form)]
    [(load-impl ctx ?ns ?args) true]
    [ctx]))
