; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns arglists)

(defn normal [a] a)

(defn docstrings
  "This is a docstring"
  [a] a)

(defn pre-attr-map
  {:arg 1}
  [a] a)

(defn post-attr-map
  ([a] a)
  {:arg 1})

(defn rest-args
  [a b & c]
  (apply + a b c))

(defn rest-multiple-bodies
  ([a b] (+ a b))
  ([a b & c]
   (apply + a b c)))

(defn destructuring
  [{:keys [a b c]}]
  (+ a b c))

(defn wrapped-body ([a] a))

(defn multiple-bodies ([a] a) ([a b] (+ a b)))

(defn multiple-bodies-docstrings
  "This is a docstring"
  ([a] a)
  ([a b] (+ a b)))

(defn arglist-metadata
  {:arglists '([a] [a b] [a b c])}
  [& args] (apply + args))

#_:clj-kondo/ignore
(defn error-bad-args (a) a)

#_:clj-kondo/ignore
(defn error-empty)

#_:clj-kondo/ignore
(defn error-bad-docstring
  [a] "asdf" a)
