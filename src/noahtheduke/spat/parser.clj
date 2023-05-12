; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser
  (:require
    [clojure.string :as str]
    [edamame.core :as e]
    [edamame.impl.read-fn :as read-fn]
    [noahtheduke.spat.ns-parser :refer [parse-ns]]))

(set! *warn-on-reflection* true)

(defn- sigs
  "Adapted from clojure.core/sigs"
  [fdecl]
  (let [asig 
        (fn [fdecl]
          (let [arglist (first fdecl)
                ;elide implicit macro args
                arglist (if (= '&form (first arglist)) 
                          (subvec arglist 2 (count arglist))
                          arglist)
                body (next fdecl)]
            (if (map? (first body))
              (if (next body)
                (with-meta arglist (conj (or (meta arglist) {}) (first body)))
                arglist)
              arglist)))]
    (if (seq? (first fdecl))
      (loop [ret [] fdecls fdecl]
        (if fdecls
          (recur (conj ret (asig (first fdecls))) (next fdecls))
          (seq ret)))
      (list (asig fdecl)))))

(defn parse-defn
  "Adapted from clojure.core but returns `nil` if given an improperly formed defn."
  [form]
  (let [fdecl (next form)
        fname (when (symbol? (first fdecl)) (first fdecl))]
    (when fname
      (let [fdecl (next fdecl)
            m {:spat/name fname}
            m (if (string? (first fdecl))
                (assoc m :doc (first fdecl))
                m)
            fdecl (if (string? (first fdecl)) (next fdecl) fdecl)
            m (if (map? (first fdecl))
                (conj m (first fdecl))
                m)
            fdecl (if (map? (first fdecl)) (next fdecl) fdecl)
            fdecl (cond
                    ;; For linting purposes, it's helpful to track the location
                    ;; of function "arities" (the arg vector plus fn body). If
                    ;; the function has single or multiple arities but they're
                    ;; all wrapped in lists, then they'll have location data
                    ;; already. However, if it's a single arity function, the
                    ;; "arity" won't have location data as it's a plain seq
                    ;; built from calling `next` repeatedly. Therefore, we
                    ;; gotta do it ourselves here.
                    ;;
                    ;; At this point, fdecl is either:
                    ;; - a single arity: ([] 1 2 3) 
                    ;; - a single arity wrapped in a list: (([] 1 2 3))
                    ;; - multiple arities (each wrapped in a list): (([] 1 2 3) ([a] a 1 2 3))
                    ;; If it's the first, that means when we create the wrapped
                    ;; version, we don't carry forward the metadata of the
                    ;; "body" (arglist plus actual body).
                    ;;
                    ;; To do that, we have to convert the fdecl seq to
                    ;; a concrete list, and attach to it the position of the
                    ;; vector at the start and one less than the position of
                    ;; the function's form at the end (because ends are
                    ;; exclusive indices).
                    ;;         start   end
                    ;;           v      v
                    ;; (defn foo [] 1 2 3)
                    (vector? (first fdecl))
                    (let [vm (meta (first fdecl))
                          loc {:line (:line vm)
                               :column (:column vm)
                               :end-row (:end-row (meta form))
                               :end-col (dec (:end-col (meta form)))}]
                      (-> (apply list fdecl)
                          (vary-meta (fnil conj {}) loc)
                          (list)))
                    ;; Otherwise, just use the existing list (which will have
                    ;; location data already).
                    (list? (first fdecl)) fdecl)
            m (assoc m :arities fdecl)
            m (when fdecl
                (if (contains? m :arglists)
                  m
                  (assoc m :arglists (sigs fdecl))))
            m (when m
                (conj (or (meta fname) {}) m))]
        m))))

(defn attach-defn-meta [obj]
  (if-let [defn-form (parse-defn obj)]
    (vary-meta obj assoc :spat/defn-form defn-form)
    obj))

(defn attach-import-meta [obj ns-state]
  (if-let [ns_ (and (symbol? obj) (some-> obj namespace symbol))]
    (if-let [fqns (get-in @ns-state [:imports ns_])]
      (vary-meta obj assoc :spat/import-ns fqns)
      obj)
    obj))

(defn make-edamame-opts [ns-state]
  {:all true
   :row-key :line
   :col-key :column
   :end-location true
   :features #{:clj :cljs}
   :read-cond :allow
   :readers (fn reader [r] (fn reader-value [v] (list 'splint/tagged-literal (list r v))))
   :auto-resolve (fn auto-resolve [ns-str]
                   (if-let [resolved-ns (get-in @ns-state [:aliases ns-str])]
                     resolved-ns
                     (if (= :current ns-str)
                       "splint-auto-current_"
                       (str "splint-auto-alias_" (name ns-str)))))
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
                    (e/iobj? obj)
                    (-> (vary-meta merge loc)
                        (attach-import-meta ns-state))
                    (and (list? obj)
                         (symbol? (first obj))
                         (str/starts-with? (name (first obj)) "defn"))
                    (attach-defn-meta)))
   :uneval (fn [{:keys [uneval next]}]
             (cond
               (identical? uneval :splint/disable)
               (vary-meta next assoc :splint/disable true)
               (and (seqable? (:splint/disable uneval))
                    (seq (:splint/disable uneval)))
               (vary-meta next assoc :splint/disable (seq (:splint/disable uneval)))
               :else
               next))
   ; All reader macros should be a splint-specific symbol wrapping the expression
   :dispatch {; @x
              \@ (fn [expr] (list 'splint/deref expr))
              ; `(+ 1 2)
              ; This is a deliberate decision to not expand syntax quotes. Easier to parse/manipulate the entire tree.
              \` (fn [expr] (list 'splint/syntax-quote expr))
              \~ {; ~x unquote
                  :default (fn [expr] (list 'splint/unquote expr))
                  ; ~@(map inc [1 2 3])
                  \@ (fn [expr] (list 'splint/unquote-splicing expr))}
              \# {; #'x
                  \' (fn [expr] (list 'splint/var expr))
                  ; #=(+ 1 2)
                  \= (fn [expr] (list 'splint/read-eval expr))
                  ; #()
                  \( (fn [expr]
                       (let [sexp (read-fn/read-fn expr)]
                         (apply list (cons 'splint/fn (next sexp)))))
                  ; #".*"
                  \" (fn [expr] (list 'splint/re-pattern expr))}}})

(defn parse-string
  ([s] (parse-string s (atom {})))
  ([s ns-state] (e/parse-string s (make-edamame-opts ns-state))))

(defn parse-string-all
  ([s] (parse-string-all s (atom {})))
  ([s ns-state] (e/parse-string-all s (make-edamame-opts ns-state))))
