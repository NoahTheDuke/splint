; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    matcher-combinators.test
    [noahtheduke.spat.pattern :refer [simple-type]]
    [noahtheduke.splint.config :refer [read-default-config deep-merge]]
    [noahtheduke.spat.parser :refer [parse-string]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :refer [check-and-recur check-form prepare-context prepare-rules]]))

(set! *warn-on-reflection* true)

(defn make-rules
  ([] (make-rules nil))
  ([test-config]
   (prepare-rules (deep-merge (read-default-config) test-config)
                  (or @global-rules {}))))

(defn check-str
  ([s] (check-str s nil))
  ([s config]
   (let [rules (make-rules config)
         ctx (prepare-context rules nil)
         form (parse-string s)]
     (seq (:diagnostics (check-form ctx (rules (simple-type form)) nil form))))))

(defn check-alt
  ([s] (check-alt s nil))
  ([s config]
   (:alt (first (check-str s config)))))

(defn check-message
  ([s] (check-message s nil))
  ([s config]
   (:message (first (check-str s config)))))

(defn check-all
  ([s] (check-all s nil))
  ([s config]
   (let [rules (make-rules config)
         ctx (prepare-context rules nil)
         form (parse-string s)]
     (check-and-recur ctx rules "filename" nil form)
     (seq @(:diagnostics ctx)))))

(defmacro with-out-str-data-map
  "Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter. Returns the result of the body and the string created by any
  nested printing calls in a map under the respective keys :result and :str."
  [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [*out* s#]
       (let [r# (do ~@body)]
         {:result r#
          :str (str s#)}))))
