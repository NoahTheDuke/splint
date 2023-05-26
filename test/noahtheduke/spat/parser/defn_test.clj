; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.spat.parser.defn-test
  (:require
    [clojure.java.io :as io]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.spat.parser :refer [parse-string-all]]
    [noahtheduke.spat.parser.defn :as sut]))

(defexpect parse-defn-arglist-metadata-test
  (expect
    (match?
      '[{:spat/name normal
         :arities (([a] a))
         :arglists ([a])}
        {:spat/name docstrings
         :doc "This is a docstring"
         :arities (([a] a))
         :arglists ([a])}
        {:spat/name pre-attr-map
         :arg 1
         :arities (([a] a))
         :arglists ([a])}
        {:spat/name post-attr-map
         :arg 1
         :arities (([a] a))
         :arglists ([a])}
        {:spat/name rest-args
         :arities (([a b & c] (apply + a b c)))
         :arglists ([a b & c])}
        {:spat/name rest-multiple-bodies
         :arities (([a b] (+ a b)) ([a b & c] (apply + a b c)))
         :arglists ([a b] [a b & c])}
        {:spat/name destructuring
         :arities (([{:keys [a b c]}] (+ a b c)))
         :arglists ([{:keys [a b c]}])}
        {:spat/name wrapped-body
         :arities (([a] a))
         :arglists ([a])}
        {:spat/name multiple-bodies
         :arities (([a] a) ([a b] (+ a b)))
         :arglists ([a] [a b])}
        {:spat/name arglist-metadata
         :arglists ([a] [a b] [a b c])
         :arities (([& args] (apply + args)))}]
      (->> (io/file "corpus" "arglists.clj")
           (slurp)
           (parse-string-all)
           (keep sut/parse-defn)))))
