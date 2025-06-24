; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.parser-test
  (:require
   [lazytest.core :refer [defdescribe describe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [matcher-combinators.matchers :refer [absent]]
   [noahtheduke.splint.parser.defn :refer [parse-defn]]
   [noahtheduke.splint.test-helpers :refer [parse-string parse-string-all]]
   [clojure.java.io :as io]
   [noahtheduke.splint.parser :refer [parse-file]]))

(set! *warn-on-reflection* true)

(defdescribe parse-string-test
  (describe parse-file
    (describe "tagged literals"
      (it "can parse unknown literals"
        (expect (=
                 '[{splint/tagged-literal (sql/raw [1 2 3])}
                   {splint/tagged-literal (unknown [4])}]
                 (parse-string-all "#sql/raw [1 2 3] #unknown [4]")))))

    (describe "auto-resolving keywords"
      (it "can parse unknown aliases"
        (expect
          (match? '[:splint-auto-current/foo :splint-auto-alias-foo/bar :foo :foo/bar]
            (parse-string-all "::foo ::foo/bar :foo :foo/bar"))))
      (it "can parse known aliases"
        (expect
          (match? '[(ns foo (:require [clojure.set :as set])) :clojure.set/foo]
            (parse-string-all "(ns foo (:require [clojure.set :as set])) ::set/foo"))))
      (it "can set the current namespace"
        (expect
          (match? '[(ns foo) :foo/bar]
            (parse-string-all "(ns foo) ::bar")))))

    (describe "discard forms"
      (it "attaches keywords to metadata"
        (expect
          (match? {:splint/disable true}
                  (meta (parse-string "#_:splint/disable (foo bar)")))))
      (it "attaches maps as metadata"
        (expect
          (match? '{:splint/disable [lint]}
                  (meta (parse-string "#_{:splint/disable [lint]} (foo bar)"))))))

    (describe "parsing defn forms"
      (it "returns the defn as data"
        (expect '(defn example "docstring" [] (+ 1 1))
          (parse-string "(defn example \"docstring\" [] (+ 1 1))")))
      (it "attaches defn form metadata"
        (expect
          (match? '{:splint/defn-form
                    {:splint/name example
                     :doc "docstring"
                     :arities (([] (+ 1 1)))
                     :arglists ([])}}
                  (meta (parse-string "(defn example \"docstring\" [] (+ 1 1))"))))
        (expect
          (match? {:splint/defn-form absent}
                  (meta (parse-string "(defn-partial abc (+ 1 2 3))"))))))

    (describe "Clojure 1.12 :param-tags"
      (let [ret (parse-string "(map ^[int] Integer/hash (range 10))")]
        (it "returns the list as data"
          (expect '(map Integer/hash (range 10)) ret))
        (it "attaches the :param-tags as metadata"
          (expect (match? {:param-tags ['int]} (meta (second ret)))))))))

(defdescribe parse-defn-test
  (describe parse-defn
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
               (keep parse-defn)))))))
