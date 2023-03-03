(ns noahtheduke.spat.rules.naming.record-name
  (:require
    [clojure.string :as str]
    [noahtheduke.spat.rules :refer [defrule ->violation]]))

(defn lower-case-name? [sexp]
  (let [record-name (str sexp)]
    (= (subs record-name 0 1)
       (str/lower-case (subs record-name 0 1)))))

(defrule record-name
  "Records should use PascalCase.

  Examples:

  # bad
  (defrecord foo [a b c])

  # good
  (defrecord Foo [a b c])
  "
  {:pattern '(defrecord ?record-name &&. ?args)
   :message ""
   :on-match (fn [rule form {:syms [?record-name ?args]}]
               (when (lower-case-name? ?record-name)
                 (let [new-record-name (symbol
                                         (str (subs (str/upper-case (str ?record-name)) 0 1)
                                              (subs (str ?record-name) 1)))
                       new-form (list* 'defrecord new-record-name ?args)]
                   (->violation rule form {:replace-form new-form}))))})
