; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.defmethod-names
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]
   [noahtheduke.splint.utils :refer [simple-type]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn build-method-ending
  [?dispatch]
  (case (simple-type ?dispatch)
    :nil "nil"
    :boolean (str ?dispatch)
    :char (str "char-" ?dispatch)
    :number (-> (str ?dispatch)
                (str/replace #"[./]" "_"))
    :string ?dispatch
    (:keyword :symbol)
    (str (namespace ?dispatch)
         (when (seq (namespace ?dispatch))
           "-")
         (name ?dispatch))
    :map
    (when-not (contains? ?dispatch 'splint/tagged-literal)
      (->> ?dispatch
           (mapcat identity)
           (map build-method-ending)
           (str/join "-")))
    :list
    (condp contains? (first ?dispatch)
      #{'quote 'splint/quote} (build-method-ending (fnext ?dispatch))
      #{'re-pattern 'splint/re-pattern} (str "regex-" (second ?dispatch))
      #_:else (->> ?dispatch
                   (map build-method-ending)
                   (str/join "-")))
    (:set :vector)
    (->> ?dispatch
         (map build-method-ending)
         (str/join "-"))
    #_:else (-> (str (class ?dispatch))
                (str/replace #"[./]" "_"))))

(defrule lint/defmethod-names
  "When defining methods for a multimethod, everything after the dispatch-val is given directly to `fn`. This allows for providing a name to the defmethod function, which will make stack traces easier to read.

  @examples

  ; avoid
  (defmethod some-multi :foo
    [arg1 arg2]
    (+ arg1 arg2))

  ; prefer
  (defmethod some-multi :foo
    some-multi--foo
    [arg1 arg2]
    (+ arg1 arg2))
  "
  {:pattern '(defmethod ?multi ?dispatch (?* args))
   :message "Include a name for the method."
   :on-match (fn [ctx rule form {:syms [?multi ?dispatch ?args]}]
               (when-not (symbol? (first ?args))
                 (let [ending (build-method-ending ?dispatch)
                       method-name (symbol (str ?multi "--" ending))
                       new-form (list* 'defmethod ?multi ?dispatch method-name ?args)]
                   (when ending
                     (->diagnostic ctx rule form {:replace-form new-form})))))})
