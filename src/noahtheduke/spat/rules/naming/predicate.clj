(ns noahtheduke.spat.rules.naming.predicate
  (:require
    [noahtheduke.spat.rules :refer [defrule ->violation]]
    [clojure.string :as str]))

(defn bad-name? [?name]
  (let [?name (str ?name)]
    (or (str/starts-with? ?name "is-")
        (str/ends-with? ?name "-p"))))

(defn good-name [?name]
  (let [?name (str ?name)]
    (cond
      (str/starts-with? ?name "is-") (str (subs ?name 3) "?")
      (str/ends-with? ?name "-p") (str (subs ?name 0 (- (count ?name) 2)) "?"))))

(defrule predicate
  "Functions that return a boolean should end in a question mark.

  Doesn't verify the kind of function, just checks for anti-patterns in the
  names.

  Examples:

  # bad
  (defn palindrome-p ...)
  (defn is-palindrome ...)

  # good
  (defn palindrome? ...)
  "
  {:pattern '(defn ?name &&. ?args)
   :message "Use a question mark instead of other language idioms."
   :on-match (fn [rule form {:syms [?name ?args]}]
               (when (bad-name? ?name)
                 (let [new-form (list* 'defn (symbol (good-name ?name)) ?args)]
                   (->violation rule form {:replace-form new-form}))))})
