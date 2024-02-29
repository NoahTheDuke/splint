; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.style.reduce-str
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn str-empty? [form]
  (.equals "" form))

(defrule style/reduce-str
  "`reduce` calls the provided function on every element in the provided
  collection. Because of how `str` is implemented, a new string is created
  every time it's called. Better to rely on `clojure.string/join`'s efficient
  StringBuilder and collection traversal.

  Additionally, the 2-arity form of `reduce` returns the first item without
  calling `str` on it if it only has one item total, which is
  generally not what is expected when calling `str` on something.

  Examples:

  ; bad
  (reduce str x)
  (reduce str \"\" x)

  ; good
  (clojure.string/join x)
  "
  {:pattern '(reduce str (?? _ str-empty?) ?coll)
   :message "Use `clojure.string/join` for efficient string concatenation."
   :replace '(clojure.string/join ?coll)})
