; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser
  (:require
    [clojure.string :as str]
    [edamame.core :as e]
    [edamame.impl.read-fn :as read-fn]))

(set! *warn-on-reflection* true)

(def base-edamame-opts
  {:all true
   :row-key :line
   :col-key :column
   :end-location true
   :location? seq?
   :features #{:cljs}
   :read-cond :preserve
   :readers (fn [r] (fn [v] (list (if (namespace r) r (symbol "splint-auto" (name r))) v)))
   ; All reader macros should be a splint-specific symbol wrapping the expression
   :dispatch {; @x
              \@ (fn [expr] (list 'splint/deref expr))
              ; `(+ 1 2)
              \` (fn [expr] (list 'splint/syntax-quote expr))
              \~ {; ~x unquote
                  :default (fn [expr] (list 'splint/unquote expr))
                  ; ~@(map inc [1 2 3])
                  \@ (fn [expr] (list 'splint/unquote-splice expr))}
              \# {; #'x
                  \' (fn [expr] (list 'splint/var expr))
                  ; #=(+ 1 2)
                  \= (fn [expr] (list 'splint/read-eval expr))
                  ; #()
                  \( (fn [expr]
                       (let [sexp (read-fn/read-fn expr)]
                         (apply list (cons 'splint/fn (next sexp)))))
                  ; #".*"
                  \" (fn [expr] (list 'splint/regex expr))}}})

(defn iobj?
  "Adapted from edamame.impl.parser."
  [x]
  (instance? clojure.lang.IObj x))

(defn make-postprocess
  "Adapted from edamame.impl.parser."
  [ns-state]
  (fn postprocess [{:keys [obj loc]}]
    (when-let [ns-parsed (when (and (seq? obj)
                                    (= 'ns (first obj)))
                           (try (e/parse-ns-form obj)
                                (catch Exception _ nil)))]
      (reset! ns-state (assoc (:aliases ns-parsed) :current (:name ns-parsed))))
    ;; Gotta apply location data here as using `:postprocess` skips automatic
    ;; location data
    (if (iobj? obj) (vary-meta obj merge loc) obj)))

(defn make-edamame-opts
  "Because of boxing, I need to perform `:auto-resolve-ns` myself. I can't
  access the `:ns-state` on edamame's `ctx`, so instead make a new `ns-state`
  each invocation of [[parse-string]] and [[parse-string-all]] to allow for
  parallel parsing."
  []
  (let [ns-state (atom {})]
    (-> base-edamame-opts
        (assoc :auto-resolve
               (fn [k] (if-let [resolved-ns (@ns-state k)]
                         resolved-ns
                         (if (= :current k)
                           "splint-auto_"
                           (str "splint-auto_" (name k))))))
        (assoc :postprocess (make-postprocess ns-state)))))

(defn convert-splint-ignore [s]
  (str/replace s #"#_(\{)?:splint/disable" "^$1:splint/disable"))

(comment
  (convert-splint-ignore "asdf #_{:splint/disable [lint]} (+ 1 x)"))

(defn parse-string [s]
  (-> s
      (convert-splint-ignore)
      (e/parse-string (make-edamame-opts))))

(defn parse-string-all [s]
  (-> s
      (convert-splint-ignore)
      (e/parse-string-all (make-edamame-opts))))
