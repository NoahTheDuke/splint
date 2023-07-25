# Todo

* [x] Move spat.parser to splint.parser.
* [x] Find a new home for simple-type.
* [x] Read deps.edn file and check the src paths by default.
* [] Research making `runner/check-files!` a pure function.

## Rule ideas

* [] Require that `cond` includes `:else nil` if left out.
* [] `(if (seq x) (...) [])` -> `(when (seq x) ...)`
* [] `(if (seq x) x (...))` -> `(or (not-empty x) ...)`
* [] Warn on mixing `->` and `->>`. Suggest `as->`?
* [] Duplicate case test entry: `(case x :a 1 :a 2 :b 3)`
* [] Quoted case entry: `(case x 'a 1)`
* [] Check protocol method varargs: `(defprotocol Foo (bar [x & xs]))`

performance genre

* [x] `(assoc m :k1 v1 :k2 v2 ...)` -> `(-> m (assoc :k1 v1) (assoc :k2 v2) ...)`
* [x] `(get-in m [:k1 :k2 :k3])` -> `(-> m :k1 :k2 :k3)`
* [x] `(get m :k)` -> `(:k m)`
* [x] `(satisfies? Foo :bar)` -> disallow
