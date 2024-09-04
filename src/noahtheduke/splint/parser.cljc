; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser
  (:require
   [clojure.string :as str]
   clojure.tools.reader.reader-types
   [edamame.core :as e]
   [edamame.impl.read-fn :as read-fn]
   #?@(:bb []
       :clj [[flatland.ordered.map :as om]])
   #?@(:bb []
       :clj [[flatland.ordered.set :as os]])
   [noahtheduke.splint.parser.defn :refer [parse-defn]]
   [noahtheduke.splint.parser.ns :refer [parse-ns]]
   [noahtheduke.splint.vendor :refer [default-imports]]))

(set! *warn-on-reflection* true)

(defn- get-fqns [ns-state ns_]
  (or (get-in @ns-state [:imports ns_])
    (get default-imports ns_)))

(defn- attach-import-meta [obj ns-state]
  (if-let [ns_ (and (symbol? obj)
                 (some-> (or (namespace obj) (name obj))
                   (str/split #"\.")
                   last
                   symbol))]
    (if-let [fqns (get-fqns ns-state ns_)]
      (vary-meta obj assoc :splint/import-ns fqns)
      obj)
    obj))

(defn- attach-defn-meta [obj]
  (if-let [defn-form (parse-defn obj)]
    (vary-meta obj assoc :splint/defn-form defn-form)
    obj))

(deftype ParseMap [elements])
(deftype ParseSet [elements])

(defn throw-dup-keys
  [kind ks]
  (letfn [(duplicates [seq]
            (for [[id freq] (frequencies seq)
                  :when (> freq 1)]
              id))]
    (let [dups (duplicates ks)]
      (apply str (str/capitalize (name kind)) " literal contains duplicate key"
        (when (> (count dups) 1) "s")
        ": " (interpose ", " dups)))))

(defn parse-map
  [^ParseMap obj loc]
  (let [elements (.elements obj)
        c (count elements)]
    (when (pos? c)
      (when (odd? c)
        (throw (ex-info (str "The map literal starting with "
                          (let [s (pr-str (first elements))]
                            (subs s 0 (min 20 (count s))))
                          " contains "
                          (count elements)
                          " form(s). Map literals must contain an even number of forms.")
                 {:type :edamame/error
                  :line (:line loc)
                  :column (:column loc)})))
      (let [ks (take-nth 2 elements)]
        (when-not (apply distinct? ks)
          (throw (ex-info (throw-dup-keys :map ks)
                   {:type :edamame/error
                    :line (:line loc)
                    :column (:column loc)})))))
    (apply #?(:bb hash-map :clj om/ordered-map) elements)))

(defn parse-set
  [^ParseSet obj loc]
  (let [elements (.elements obj)
        the-set (apply #?(:bb hash-set :clj os/ordered-set) elements)]
    (when-not (= (count elements) (count the-set))
      (throw (ex-info (throw-dup-keys :set elements)
               {:type :edamame/error
                :line (:line loc)
                :column (:column loc)})))
    the-set))

(defn- make-edamame-opts [{:keys [features ext ns-state]
                           :or {ns-state (atom {})}}]
  {:all true
   :row-key :line
   :col-key :column
   :end-row-key :end-line
   :end-col-key :end-column
   :end-location true
   :features features
   :read-cond :allow
   :readers (fn reader [r]
              (fn reader-value [v]
                (let [tag-meta {:ext ext}
                      tag (vary-meta 'splint/tagged-literal merge tag-meta)]
                  {tag (list r v)})))
   :auto-resolve (fn auto-resolve [ns-str]
                   (if-let [resolved-ns (get-in @ns-state [:aliases ns-str])]
                     resolved-ns
                     (if (= :current ns-str)
                       "splint-auto-current"
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
                      (attach-import-meta ns-state))
                    (and (list? obj)
                      (symbol? (first obj))
                      (symbol? (second obj))
                      (#{"defn" "defn-"} (name (first obj))))
                    (attach-defn-meta)))
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
  (parse-file {:contents "#=(+ 1 1)"}))
