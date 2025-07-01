; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.catch-throwable
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.rules.helpers :refer [symbol-class?]]))

(set! *warn-on-reflection* true)

(defn get-class-name
  [klass]
  (some-> klass
    (str)
    (str/split #"\.")
    (last)
    (symbol)))

(defrule lint/catch-throwable
  "Throwable is a superclass of all Errors and Exceptions in Java. Catching Throwable will also catch Errors, which indicate a serious problem that most applications should not try to catch. If there is a single specific Error you need to catch, use it directly.

  By default, only `Throwable` will raise a diagnostic. If you wish to also warn against `Error` (or any specific Throwable for that matter), it can be added with the config `:throwables []`.

  @safety
  Because there might be legitimate reasons to catch Throwable (mission-critical processes), any potential changes must be treated with care and consideration.

  @examples

  ; avoid
  (try (foo)
    (catch Throwable t ...))

  ; prefer
  (try (foo)
    (catch ExceptionInfo ex ...)
    (catch AssertionError t ...))
  "
  {:pattern '(catch (? throwable symbol-class?) ?*args)
   :ext :clj
   :config-coercer (fn throwable-coercer [config]
                     (update config :throwables #(into (set %) (map get-class-name) %)))
   :on-match (fn [ctx rule form {:syms [?throwable ?args]}]
               (let [?throwable (-> ?throwable str (str/split #"\.") last symbol)]
                 (when (contains? (:throwables (:config rule)) ?throwable)
                   (let [message (if (#{'Throwable 'Error 'Exception} ?throwable)
                                   (format "%s is too broad to safely catch." ?throwable)
                                   (format "Catching %s is disallowed." (str ?throwable)))]
                     (->diagnostic ctx rule form {:message message})))))})
