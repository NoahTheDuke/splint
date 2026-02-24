; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser
  (:require
   [clojure.string :as str]
   clojure.tools.reader.reader-types
   [edamame.core :as e]
   [edamame.impl.read-fn :as read-fn]
   [noahtheduke.splint.clojure-ext.core :refer [parse-bigint parse-map
                                                parse-set vary-meta*]]
   [noahtheduke.splint.parser.defmulti :refer [parse-defmulti]]
   [noahtheduke.splint.parser.defn :refer [parse-defn]]
   [noahtheduke.splint.parser.ns :refer [parse-ns]]
   [noahtheduke.splint.vendor :refer [default-imports]])
  (:import
   (clojure.lang BigInt)
   (noahtheduke.splint.clojure_ext.core ParseMap ParseSet)))

(set! *warn-on-reflection* true)

(defn- get-imported-klass [ns-state klass]
  (when klass
    (or (get-in @ns-state [:imports klass])
      (get default-imports klass))))

(defn- attach-ns-meta [obj ns-state]
  (if (symbol? obj)
    (let [obj-ns (namespace obj)
          klass (some-> (or obj-ns (name obj))
                  (str/split #"\.")
                  last
                  symbol)
          imported-klass (get-imported-klass ns-state klass)
          require-ns (when obj-ns (get-in @ns-state [:aliases (symbol obj-ns)]))]
      (cond-> obj
        imported-klass (vary-meta assoc :splint/import-ns imported-klass)
        require-ns (vary-meta assoc :splint/origin-ns require-ns)))
    obj))

(defn- attach-defn-meta [obj]
  (if-let [defn-form (parse-defn obj)]
    (vary-meta obj assoc :splint/defn-form defn-form)
    obj))

(defn- attach-defmulti-meta [obj]
  (if-let [defmulti-form (parse-defmulti obj)]
    (vary-meta obj assoc :splint/defmulti-form defmulti-form)
    obj))

(defn- make-edamame-opts [{:keys [features ext ns-state]
                           :or {ns-state (atom {})}}]
  {:all true
   :row-key :line
   :col-key :column
   :end-row-key :end-line
   :end-col-key :end-column
   :end-location true
   :features features
   :read-cond (fn read-cond [obj]
                (let [pairs (partition 2 obj)]
                  (loop [pairs pairs]
                    (if (seq pairs)
                      (let [[k v] (first pairs)]
                        (if (or (contains? features k)
                                (= k :default))
                          (cond-> v
                            true (vary-meta* assoc :splint/reader-cond true)
                            (:edamame/read-cond-splicing (meta obj))
                            (vary-meta* assoc :edamame.impl.parser/cond-splice true))
                          (recur (next pairs))))
                      e/continue))))
   :readers (fn reader [r]
              (fn reader-value [v]
                (let [tag-meta {:ext ext}
                      tag (vary-meta 'splint/tagged-literal merge tag-meta)]
                  {tag (list r v)})))
   :auto-resolve (fn auto-resolve [ns-str]
                   (if-let [resolved-ns (get-in @ns-state [:aliases ns-str])]
                     resolved-ns
                     (if (= :current ns-str)
                       (or (:current @ns-state) "splint-auto-current")
                       (str "splint-auto-alias-" (name ns-str)))))
   :postprocess (fn postprocess [{:keys [obj loc]}]
                  (when-let [{:keys [current aliases imports]} (parse-ns obj)]
                    (when current
                      (reset! ns-state {:current current}))
                    (when aliases
                      (swap! ns-state update :aliases merge aliases))
                    (when imports
                      (swap! ns-state update :imports merge imports)))
                  ;; Gotta apply location data here as using `:postprocess`
                  ;; skips automatic location data
                  (cond-> obj
                    (instance? ParseMap obj) (parse-map loc)
                    (instance? ParseSet obj) (parse-set loc)
                    (instance? clojure.lang.IObj obj)
                    (-> (vary-meta merge loc)
                      (attach-ns-meta ns-state))
                    (and (list? obj)
                      (symbol? (first obj))
                      (symbol? (second obj))
                      (#{"defn" "defn-"} (name (first obj))))
                    (attach-defn-meta)
                    (and (list? obj)
                      (symbol? (first obj))
                      (symbol? (second obj))
                      (String/.equals "defmulti" (name (first obj))))
                    (attach-defmulti-meta)
                    ;; last because it will be rare
                    (instance? BigInt obj) (parse-bigint)))
   ; Each of dispatch literals should either be processed (uneval), or wrap the
   ; expression in a splint-specific "function call".
   ; @x
   :deref (fn [expr] (list 'splint/deref expr))
   ; #()
   :fn (fn [expr]
         (let [sexp (read-fn/read-fn expr)]
           (apply list (cons 'splint/fn (next sexp)))))
   ; {}
   :map (fn [& elements] (ParseMap. elements))
   ; #=(+ 1 2)
   :read-eval (fn [expr] (list 'splint/read-eval expr))
   ; #".*"
   :regex (fn [expr] (list 'splint/re-pattern expr))
   ; #{}
   :set (fn [& elements] (ParseSet. elements))
   ; #'x
   :var (fn [expr] (list 'splint/var expr))
   ; #_
   :uneval (fn [{:keys [uneval next]}]
             (cond
               (identical? uneval :splint/disable)
               (vary-meta next assoc :splint/disable true)
               (and (seqable? (:splint/disable uneval))
                 (seq (:splint/disable uneval)))
               (vary-meta next assoc :splint/disable (seq (:splint/disable uneval)))
               :else
               next))
   ; `(+ 1 2)
   :syntax-quote (fn [expr] (list 'splint/syntax-quote expr))
   ; ~x unquote
   :unquote (fn [expr] (list 'splint/unquote expr))
   ; ~@(map inc [1 2 3])
   :unquote-splicing (fn [expr] (list 'splint/unquote-splicing expr))})

(defn parse-file
  [file-obj]
  (e/parse-string-all (:contents file-obj) (make-edamame-opts file-obj)))

(comment
  (parse-file {:contents "#?(:clj foo :cljs bar)" :features #{:clj}}))
