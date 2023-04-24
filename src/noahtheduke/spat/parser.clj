; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser
  (:require
    [edamame.core :as e]
    [edamame.impl.read-fn :as read-fn]
    [noahtheduke.spat.ns-parser :refer [parse-ns]]))

(set! *warn-on-reflection* true)

(defn attach-import-meta [ns-state obj]
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
                  ;; Gotta apply location data here as using `:postprocess` skips automatic
                  ;; location data
                  (if (e/iobj? obj)
                    (->> (vary-meta obj merge loc)
                         (attach-import-meta ns-state))
                    obj))
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
