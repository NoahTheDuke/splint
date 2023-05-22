; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.test-helpers
  (:require
    noahtheduke.splint
    noahtheduke.splint.rules.helpers
    matcher-combinators.test
    [clojure.spec.alpha :as s]
    [expectations.clojure.test :refer [expect]]
    [noahtheduke.spat.parser :refer [parse-string]]
    [noahtheduke.spat.pattern :refer [drop-quote]]
    [noahtheduke.splint.config :refer [deep-merge read-default-config]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :refer [check-and-recur prepare-context prepare-rules]]))

(set! *warn-on-reflection* true)

(def default-config (atom (read-default-config)))

(defn make-rules
  ([] (make-rules nil))
  ([test-config]
   (prepare-rules (deep-merge @default-config test-config)
                  (or @global-rules {}))))

(defn- check-all
  ([s] (check-all s nil))
  ([s config]
   (let [rules (make-rules config)
         ctx (prepare-context rules nil)
         form (parse-string s)]
     (check-and-recur ctx rules
                      (or (:filename config) "filename")
                      (:parent-form (meta form))
                      form)
     (seq @(:diagnostics ctx)))))

(defmacro expect-match
  ([expected s] `(expect-match ~expected ~s nil))
  ([expected s config]
   `(let [diagnostics# (#'check-all ~s ~config)]
      (expect (~'match? ~expected diagnostics#)))))

(s/fdef expect-match
        :args (s/cat :expected (s/or :nil nil? :vector #(vector? (drop-quote %)))
                     :s any?
                     :config (s/? (s/or :nil nil?
                                        :sym #(symbol? (drop-quote %))
                                        :map #(map? (drop-quote %)))))
        :ret any?)

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
