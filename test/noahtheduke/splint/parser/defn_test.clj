; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser.defn-test 
  (:require
   [clojure.java.io :as io]
   [lazytest.core :refer [defdescribe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.parser.defn :refer [parse-defn]]
   [noahtheduke.splint.test-helpers :refer [parse-string-all]]))

(set! *warn-on-reflection* true)

(defdescribe parse-defn-test
  (it "handles all possible forms"
    (expect
      (match?
        '[{:splint/name normal
           :arities (([a] a))
           :arglists ([a])}
          {:splint/name docstrings
           :doc "This is a docstring"
           :arities (([a] a))
           :arglists ([a])}
          {:splint/name pre-attr-map
           :arg 1
           :arities (([a] a))
           :arglists ([a])}
          {:splint/name post-attr-map
           :arg 1
           :arities (([a] a))
           :arglists ([a])}
          {:splint/name rest-args
           :arities (([a b & c] (apply + a b c)))
           :arglists ([a b & c])}
          {:splint/name rest-multiple-bodies
           :arities (([a b] (+ a b)) ([a b & c] (apply + a b c)))
           :arglists ([a b] [a b & c])}
          {:splint/name destructuring
           :arities (([{:keys [a b c]}] (+ a b c)))
           :arglists ([{:keys [a b c]}])}
          {:splint/name wrapped-body
           :arities (([a] a))
           :arglists ([a])}
          {:splint/name multiple-bodies
           :arities (([a] a) ([a b] (+ a b)))
           :arglists ([a] [a b])}
          {:splint/name multiple-bodies-docstrings
           :doc "This is a docstring"
           :arities (([a] a) ([a b] (+ a b)))
           :arglists ([a] [a b])}
          {:splint/name arglist-metadata
           :arglists ([a] [a b] [a b c])
           :arities (([& args] (apply + args)))}
          {:splint/name error-bad-docstring
           :arities (([a] "asdf" a))
           :arglists ([a])}]
        (->> (io/file "corpus" "arglists.clj")
          (slurp)
          (parse-string-all)
          (keep parse-defn))))))
