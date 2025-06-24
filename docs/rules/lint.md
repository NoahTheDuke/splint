# Lint

<!-- toc -->

- [lint/assoc-fn](#lintassoc-fn)
- [lint/body-unquote-splicing](#lintbody-unquote-splicing)
- [lint/catch-throwable](#lintcatch-throwable)
- [lint/defmethod-names](#lintdefmethod-names)
- [lint/divide-by-one](#lintdivide-by-one)
- [lint/dorun-map](#lintdorun-map)
- [lint/dot-class-method](#lintdot-class-method)
- [lint/dot-obj-method](#lintdot-obj-method)
- [lint/duplicate-case-test](#lintduplicate-case-test)
- [lint/duplicate-field-name](#lintduplicate-field-name)
- [lint/existing-constant](#lintexisting-constant)
- [lint/fn-wrapper](#lintfn-wrapper)
- [lint/identical-branches](#lintidentical-branches)
- [lint/if-else-nil](#lintif-else-nil)
- [lint/if-let-else-nil](#lintif-let-else-nil)
- [lint/if-nil-else](#lintif-nil-else)
- [lint/if-not-both](#lintif-not-both)
- [lint/if-not-do](#lintif-not-do)
- [lint/if-not-not](#lintif-not-not)
- [lint/if-same-truthy](#lintif-same-truthy)
- [lint/incorrectly-swapped](#lintincorrectly-swapped)
- [lint/into-literal](#lintinto-literal)
- [lint/let-if](#lintlet-if)
- [lint/let-when](#lintlet-when)
- [lint/locking-object](#lintlocking-object)
- [lint/loop-do](#lintloop-do)
- [lint/loop-empty-when](#lintloop-empty-when)
- [lint/min-max](#lintmin-max)
- [lint/misplaced-type-hint](#lintmisplaced-type-hint)
- [lint/missing-body-in-when](#lintmissing-body-in-when)
- [lint/no-catch](#lintno-catch)
- [lint/no-op-assignment](#lintno-op-assignment)
- [lint/not-empty?](#lintnot-empty)
- [lint/prefer-method-values](#lintprefer-method-values)
- [lint/prefer-require-over-use](#lintprefer-require-over-use)
- [lint/rand-int-one](#lintrand-int-one)
- [lint/redundant-call](#lintredundant-call)
- [lint/redundant-str-call](#lintredundant-str-call)
- [lint/require-explicit-param-tags](#lintrequire-explicit-param-tags)
- [lint/take-repeatedly](#linttake-repeatedly)
- [lint/thread-macro-one-arg](#lintthread-macro-one-arg)
- [lint/try-splicing](#linttry-splicing)
- [lint/underscore-in-namespace](#lintunderscore-in-namespace)
- [lint/warn-on-reflection](#lintwarn-on-reflection)

<!-- tocstop -->

## lint/assoc-fn

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`assoc`-ing an update with the same key is hard to read. `update` is known and
idiomatic.

### Examples

```clojure
; avoid
(assoc coll :a (+ (:a coll) 5))
(assoc coll :a (+ (coll :a) 5))
(assoc coll :a (+ (get coll :a) 5))

; prefer
(update coll :a + 5)
```

---

## lint/body-unquote-splicing

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.0           | 1.0             |

A macro that nests an `unquote-splicing` in a macro with a `& body` can lead
to subtle hard to debug errors. Better to wrap the `unquote-splicing` in
a `do` to force it into 'expression position'.

### Examples

```clojure
; avoid
`(binding [max mymax] ~@body)

; prefer
`(binding [max mymax] (let [res# (do ~@body)] res#))
```

### Reference

* <https://blog.ambrosebs.com/2022/09/08/break-your-macros.html>

---

## lint/catch-throwable

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | <<next>>      | <<next>>        |

Throwable is a superclass of all Errors and Exceptions in Java. Catching Throwable will also catch Errors, which indicate a serious problem that most applications should not try to catch. If there is a single specific Error you need to catch, use it directly.

By default, only `Throwable` will raise a diagnostic. If you wish to also warn against `Error` (or any specific Throwable for that matter), it can be added with the config `:throwables []`.

### Safety
Because there might be legitimate reasons to catch Throwable (mission-critical processes), any potential changes must be treated with care and consideration.

### Examples

```clojure
; avoid
(try (foo)
  (catch Throwable t ...))

; prefer
(try (foo)
  (catch ExceptionInfo ex ...)
  (catch AssertionError t ...))
```

### Configurable Attributes

| Name          | Default        | Options |
| ------------- | -------------- | ------- |
| `:throwables` | `#{Throwable}` | Set     |

### Reference

* <https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Error.html>

---

## lint/defmethod-names

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | false       | 1.18.0        | 1.18.0          |

When defining methods for a multimethod, everything after the dispatch-val is given directly to `fn`. This allows for providing a name to the defmethod function, which will make stack traces easier to read.

### Examples

```clojure
; avoid
(defmethod some-multi :foo
  [arg1 arg2]
  (+ arg1 arg2))

; prefer
(defmethod some-multi :foo
  some-multi--foo
  [arg1 arg2]
  (+ arg1 arg2))
```

---

## lint/divide-by-one

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Checks for `(/ x 1)`.

### Examples

```clojure
; avoid
(/ x 1)

; prefer
x
```

---

## lint/dorun-map

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.1           |

`map` is lazy, which carries a performance and memory cost. `dorun` uses `seq` iteration to realize the entire sequence, returning `nil`. This style of iteration also carries a performance and memory cost. `dorun` is intended for more complex sequences, whereas a simple `map` can be accomplished with `reduce` + `conj`.

`run!` uses `reduce` which non-lazy and has no `seq` overhead.

### Examples

```clojure
; avoid
(dorun (map println (range 10)))

; prefer
(run! println (range 10))
```

---

## lint/dot-class-method

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1           | 0.1             |

Using the `Obj/staticMethod` form maps the method call to Clojure's natural function position.

**NOTE:** This rule is disabled if `lint/prefer-method-values` is enabled to prevent conflicting diagnostics.

### Safety
This rule is unsafe, as it can misunderstand when a symbol is or is not a class.

### Examples

```clojure
; avoid
(. Obj staticMethod args)
(. Obj (staticMethod) args)

; prefer
(Obj/staticMethod args)
```

---

## lint/dot-obj-method

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1           | 1.15.2          |

Using the `.method` form maps the method symbol to Clojure's natural function position.

**NOTE:** This rule is disabled if `lint/prefer-method-values` is enabled to prevent conflicting diagnostics.

### Safety
This rule is unsafe, as it can misunderstand when a symbol is or is not a class.

### Examples

```clojure
; avoid
(. obj method args)

; prefer
(.method obj args)
```

### Configurable Attributes

| Name            | Default | Options                 |
| --------------- | ------- | ----------------------- |
| `:chosen-style` | `:dot`  | `:dot`, `:method-value` |

---

## lint/duplicate-case-test

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.16.0        | 1.16.0          |

It's an error to have duplicate `case` test constants.

### Examples

```clojure
; avoid
(case x :foo :bar :foo :baz)
```

### Reference

* <https://clojuredocs.org/clojure.core/case>
* <https://github.com/clj-kondo/clj-kondo/blob/18448daa0ca2b53b2dddce5773f641bed3b5fc85/doc/linters.md#duplicate-case-test>

---

## lint/duplicate-field-name

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.119       | 0.1.119         |

`deftype` and `defrecord` will throw errors if you define multiple fields
with the same name, but it's good to catch these things early too.

### Examples

```clojure
; avoid
(defrecord Foo [a b a])

; prefer
(defrecord Foo [a b c])
```

---

## lint/existing-constant

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

**NOTE:** Requires Clojure version 1.11.0.

Java has `PI` and `E` constants built-in, and `clojure.math` exposes them directly. Better to use them instead of poorly approximating them with vars.

### Examples

```clojure
; avoid
(def pi 3.14)
(def e 2.718)

; prefer
clojure.math/PI
clojure.math/E
```

---

## lint/fn-wrapper

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1           | 0.1             |

Avoid wrapping functions in pass-through anonymous function defitions.

By default, all non-interop symbols in function position are checked. However, given that many macros require wrapping, skipping them can be configured with `:names-to-skip`, which takes a vector of simple symbols to skip during analysis. For example, `lint/fn-wrapper {:names-to-skip [inspect]}` will not trigger on `(add-tap (fn [x] (morse/inspect)))`.

### Safety
This rule is unsafe, as it can misunderstand when a function is or is not a method or a macro.

### Examples

```clojure
; avoid
(fn [num] (even? num))

; prefer
even?

; avoid
(let [f (fn [num] (even? num))] ...)

; prefer
(let [f even?] ...)

; with `:names-to-skip [even?]`
; no error
(fn [num] (even? num))
```

### Configurable Attributes

| Name             | Default | Options |
| ---------------- | ------- | ------- |
| `:names-to-skip` | `#{}`   | Set     |

### Reference

* https://guide.clojure.style/#no-useless-anonymous-fns

---

## lint/identical-branches

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

Returning branches of an `if` or `cond` should not be identical. There's likely a bug in one of the branches. In `cond` branches, only checks consecutive branches as order of checks might be important otherwise.

### Examples

```clojure
; avoid
(if (pred)
  [1 2 3]
  [1 2 3])

(cond
  (pred1) [1 2 3]
  (pred2) [1 2 3]
  (other) [4 5 6])

; prefers
(cond
  (or (pred1) (pred2)) [1 2 3]
  (other) [4 5 6])

(cond
  (pred1) [1 2 3]
  (other) [4 5 6]
  (pred2) [1 2 3])
```

---

## lint/if-else-nil

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

### Examples

```clojure
; avoid
(if (some-func) :a nil)

; prefer
(when (some-func) :a)
```

### Reference

* https://guide.clojure.style/#when-instead-of-single-branch-if

---

## lint/if-let-else-nil

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Idiomatic `if-let` defines both branches. `when-let` returns `nil` in the else branch.

### Examples

```clojure
; avoid
(if-let [a 1] a nil)

; prefer
(when-let [a 1] a)
```

---

## lint/if-nil-else

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Idiomatic `if` defines both branches. `when-not` returns `nil` in the truthy branch.

### Examples

```clojure
; avoid
(if (some-func) nil :a)

; prefer
(when-not (some-func) :a)
```

---

## lint/if-not-both

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`if-not` exists, so use it.

### Examples

```clojure
; avoid
(if (not x) y z)

; prefer
(if-not x y z)
```

### Reference

* https://guide.clojure.style/#if-not

---

## lint/if-not-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`when-not` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; avoid
(if-not x (do (println :a) (println :b) :c))

; prefer
(when-not x (println :a) (println :b) :c)
```

---

## lint/if-not-not

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Two `not`s cancel each other out.

### Examples

```clojure
; avoid
(if-not (not x) y z)

; prefer
(if x y z)
```

---

## lint/if-same-truthy

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`or` exists so use it lol.

### Examples

```clojure
; avoid
(if x x y)

; prefer
(or x y)
```

---

## lint/incorrectly-swapped

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

It can be necessary to swap two variables. This usually requires an intermediate variable, but with destructuring, Clojure can perform this in a single line. However, without an intermediate variable or destructuring, manually swapping can result in both variables ending up with the same value.

### Examples

```clojure
; avoid
(let [a b
      b a] ...)

; prefer
(let [[a b] [b a]] ...)
```

---

## lint/into-literal

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.19.0          |

`vec` and `set` are succinct and meaningful.

### Examples

```clojure
; avoid
(into [] coll)

; prefer
(vec coll)

; avoid
(into #{} coll)

; prefer
(set coll)
```

---

## lint/let-if

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1.69        | 0.1.69          |

`if-let` exists so use it.

### Safety
Suggestions can be wrong as there's no code-walking to determine if `result` binding is used in falsy branch.

### Examples

```clojure
; avoid
(let [result (some-func)] (if result (do-stuff result) (other-stuff)))

; prefer
(if-let [result (some-func)] (do-stuff result) (other-stuff))
```

### Reference

* https://guide.clojure.style/#if-let

---

## lint/let-when

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1.69        | 0.1.69          |

`when-let` exists so use it.

### Safety
Suggestions can be wrong as there's no code-walking to determine if `result` binding is used in falsy branch.

### Examples

```clojure
; avoid
(let [result (some-func)] (when result (do-stuff result)))

; prefer
(when-let [result (some-func)] (do-stuff result))
```

### Reference

* https://guide.clojure.style/#when-let

---

## lint/locking-object

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.16.0        | 1.16.0          |

Synchronizing on interned objects is really bad. If multiple places lock on the same type of interned objects, those places are competing for locks.

### Examples

```clojure
; avoid
(locking :hello (+ 1 1))

; prefer
(def hello (Object.))
(locking hello (+ 1 1))
```

---

## lint/loop-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`loop` has an implicit `do`. Use it.

### Examples

```clojure
; avoid
(loop [] (do (println 1) (println 2)))

; prefer
(loop [] (println 1) (println 2))
```

---

## lint/loop-empty-when

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

Empty loops with nested `when` can be `while`. Doesn't apply if the final expr of the `when` isn't `(recur)`, which includes any nested cases (`let`, etc).

### Examples

```clojure
; avoid
(loop [] (when (some-func) (println 1) (println 2) (recur)))

; prefer
(while (some-func) (println 1) (println 2))
```

---

## lint/min-max

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

Clamping a value between two numbers requires saying at max of the lower number and a min of the higher number. If the min is lower than the max, then the min

### Examples

```clojure
; avoid
(min 10 (max 100 foo))
(max 100 (min 10 foo))

; prefer
(min 100 (max 10 foo))
```

---

## lint/misplaced-type-hint

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.20.0        | 1.20.0          |

In interop scenarios, it can be necessary to add a type hint to mark a function's return type. This can be done by adding metadata to the function's name symbol or to the function's param vector. The former works but is prone to errors and is not recommended by the core team, whereas the latter is the official method. (See links below for further discussion.)

**NOTE:** Only checks `defn` forms. (Compare with [eastwood's `:wrong-tag`](https://github.com/jonase/eastwood#wrong-tag) linter.)

### Examples

```clojure
; avoid
(defn ^String make-str
  []
  "abc")

(defn ^String make-str
  ([] "abc")
  ([a] (str a "abc")))

; prefer
(defn make-str ^String [] "abc")

(defn make-str
  (^String [] "abc")
  (^String [a] (str a "abc")))
```

### Reference

* <https://clojure.org/reference/java_interop#typehints>
* <https://clojure.org/guides/faq#return_type_hint>

---

## lint/missing-body-in-when

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 0.1.69        | 0.1.69          |

`when` calls should have at least 1 expression after the condition.

### Examples

```clojure
; avoid
(when true)
(when (some-func))

; prefer
(when true (do-stuff))
(when (some-func) (do-stuff))
```

---

## lint/no-catch

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

Try without a `catch` (or `finally`) clause is a no-op, and indicates something got changed or broken at some point.

With the default style `:accept-finally`, both `catch` and `finally` clauses are counted to see if the `try` is a no-op. The style `:only-catch` can be used to raise a warning for `(try ... (finally ...))` forms with no `catch` clauses.

### Examples

```clojure
; avoid
(try (foo))

; avoid (chosen style :only-catch)
(try (foo)
  (finally (bar)))

; prefer (chosen style :only-catch)
(try (foo)
  (catch Exception ex
    ...))

; prefer (chosen style :accept-finally (default))
(try (foo)
  (finally (bar)))

(try (foo)
  (catch Exception ex
    ...))
```

### Configurable Attributes

| Name            | Default           | Options                          |
| --------------- | ----------------- | -------------------------------- |
| `:chosen-style` | `:accept-finally` | `:accept-finally`, `:only-catch` |

---

## lint/no-op-assignment

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

If the bind is a symbol and the expr is the same symbol, just use the expr directly. (Otherwise, indicates a potential bug.)

Skips if the expr is a reader conditional or has a type-hint.

### Examples

```clojure
; avoid
(let [foo foo] ...)

; ignores
(let [foo #?(:clj foo :cljs (js-foo-getter))] ...)
(let [foo ^ArrayList foo] ...)
```

---

## lint/not-empty?

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as `(not (seq coll))` so it's idiomatic to use `seq` directly.

### Examples

```clojure
; avoid
(not (empty? coll))

; prefer (chosen style :seq (default))
(seq coll)

; prefer (chosen style :not-empty)
(not-empty coll)
```

### Configurable Attributes

| Name            | Default | Options              |
| --------------- | ------- | -------------------- |
| `:chosen-style` | `:seq`  | `:seq`, `:not-empty` |

### Reference

* https://guide.clojure.style/#nil-punning

---

## lint/prefer-method-values

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.13          | 1.13            |

**NOTE:** Requires Clojure version 1.12.0.

Uniform qualified method values are a new syntax for calling into java code. They must resolve to a single static or instance method and to help with that, a new metadata syntax can be used: `^[]` aka `^{:param-tags []}`. Types are specified with classes, each corrosponding to an argument in the target method: `(^[long String] SomeClass/.someMethod 1 "Hello world!")`. It compiles to a direct call without any reflection, guaranteeing optimal performance.

Given that, it is preferable to exclusively use method values.

### Examples

```clojure
; avoid
(.toUpperCase "noah")
(. "noah" toUpperCase)

; prefer
(^[] String/toUpperCase "noah")
```

### Reference

* <https://clojure.org/news/2024/04/28/clojure-1-12-alpha10#method_values>

---

## lint/prefer-require-over-use

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.3.0         | 1.3.0           |

In the `ns` form prefer `:require :as` over `:require :refer` over `:require :refer :all`. Prefer `:require` over `:use`; the latter form should be considered deprecated for new code.

### Examples

```clojure
; avoid
(ns examples.ns
  (:use clojure.zip))

; prefer
(ns examples.ns
  (:require [clojure.zip :as zip]))
(ns examples.ns
  (:require [clojure.zip :refer [lefts rights]]))
(ns examples.ns
  (:require [clojure.zip :refer :all]))
```

### Configurable Attributes

| Name            | Default | Options                 |
| --------------- | ------- | ----------------------- |
| `:chosen-style` | `:as`   | `:as`, `:refer`, `:all` |

### Reference

* https://guide.clojure.style/#prefer-require-over-use

---

## lint/rand-int-one

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | <<next>>      | <<next>>        |

`clojure.core/rand-int` returns an integer between `0` (inclusive) and `n` (exclusive), meaning that a call to `(rand-int 1)` will always return `0`.

Checks the following numbers: `0`, `0.0`, `1`, `1.0`, `-1`, `-1.0`

### Examples

```clojure
; avoid
(rand-int 0)
(rand-int -1)
(rand-int 1)
(rand-int 1.0)
(rand-int -1.0)
(rand-int 1.5)
```

---

## lint/redundant-call

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

A number of core functions take any number of arguments and return the arg
if given only one. These calls are effectively no-ops, redundant, so they
should be avoided.

Current list of clojure.core functions this linter checks:

* `->`, `->>`
* `cond->`, `cond->>`
* `some->`, `some->>`
* `comp`, `partial`, `merge`
* `min`, `max`, `distinct?`

### Examples

```clojure
; avoid
(-> x)
(->> x)
(cond-> x)
(cond->> x)
(some-> x)
(some->> x)
(comp x)
(partial x)
(merge x)
(min x)
(max x)
(distinct? x)

; prefer
x
```

---

## lint/redundant-str-call

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.16.0        | 1.16.0          |

`clojure.core/str` calls `.toString()` on non-nil input. However, `.toString()` on a string literal returns itself, making it a no-op. Likewise, `clojure.core/format` unconditionally returns a string, making any calls to `str` on the results a no-op.

### Examples

```clojure
; avoid
(str "foo")
(str (format "foo-%s" some-var))

; prefer
"foo"
(format "foo-%s" some-var)
```

---

## lint/require-explicit-param-tags

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | false       | 1.13          | 1.13            |

**NOTE:** Requires Clojure version 1.12.0.

Uniform qualified method values are a new syntax for calling into java code. They must resolve to a single static or instance method and to help with that, a new metadata syntax can be used: `^[]` aka `^{:param-tags []}`. Types are specified with classes, each corrosponding to an argument in the target method: `(^[long String] SomeClass/someMethod 1 "Hello world!")`

If `:param-tags` is left off of a method value, then the compiler treats it as taking no arguments (a 0-arity static method or a 1-arity instance method with the instance being the first argument). And an `_` can be used as a wild-card in the cases where there is only a single applicable method (no overloads).

These last two features are where there can be trouble. If, for whatever reason, the Java library adds an overload on type, then both the lack of `:param-tags` and a wild-card can lead to ambiguity. This is a rare occurence but risky/annoying enough that it's better to be explicit overall.

The styles are named after what they're looking for:

* `:missing` checks that there exists a `:param-tags` on a method value.
* `:wildcard` checks that there are no usages of `_` in an existing `:param-tags`.
* `:both` checks both conditions.

### Examples

```clojure
; avoid (chosen style :both or :missing)
(java.io.File/mkdir (clojure.java.io/file "a"))

; avoid (chosen style :both or :wildcard)
(^[_ _] java.io.File/createTempFile "abc" "b")

; prefer (chosen style :both or :missing)
(^[] java.io.File/mkdir (clojure.java.io/file "a"))

; prefer (chosen style :both or :wildcard (default))
(^[String String] java.io.File/createTempFile "abc" "b")
```

### Configurable Attributes

| Name            | Default     | Options                          |
| --------------- | ----------- | -------------------------------- |
| `:chosen-style` | `:wildcard` | `:both`, `:missing`, `:wildcard` |

### Reference

* <https://insideclojure.org/2024/02/12/method-values>

---

## lint/take-repeatedly

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1             |

`repeatedly` has an arity for limiting the number of repeats with `take`.

### Examples

```clojure
; avoid
(take 5 (repeatedly (range 10))

; prefer
(repeatedly 5 (range 10))
```

---

## lint/thread-macro-one-arg

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| false              | false | false       | 0.1           | 1.17.0          |

Threading macros require more effort to understand so only use them with multiple
args to help with readability.

### Safety
Macros can be misinterpreted, leading to correct code being flagged:
```clojure
(cond-> foo
  pred? (-> (assoc :hello 123)
            (dissoc :goodbye)))
```

### Examples

```clojure
; avoid
(-> x y)
(->> x y)

; prefer
(y x)

; avoid
(-> x (y z))

; prefer
(y x z)

; avoid
(->> x (y z))

; prefer
(y z x)
```

### Configurable Attributes

| Name            | Default   | Options                         |
| --------------- | --------- | ------------------------------- |
| `:chosen-style` | `:inline` | `:inline`, `:avoid-collections` |

---

## lint/try-splicing

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.0           | 1.0             |

A macro that wraps a splicing unquote in a try-catch or try-finally can lead
to subtle hard to debug errors. Better to wrap the splicing unquote in a `do`
to force it into 'expression position'.

### Examples

```clojure
; avoid
`(try ~@body (finally :true))

; prefer
`(try (do ~@body) (finally :true))
```

### Reference

* <https://blog.ambrosebs.com/2022/09/08/break-your-macros.html>

---

## lint/underscore-in-namespace

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.11          | 1.11            |

Due to munging rules, underscores in namespaces can confuse tools and libraries which expect that underscores in class names should be dashes in Clojure.

### Examples

```clojure
; avoid
(ns foo_bar.baz_qux)

; prefer
(ns foo-bar.baz-qux)
```

---

## lint/warn-on-reflection

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | false       | 1.8.0         | 1.8.0           |

Because we can't (or won't) check for interop, `*warn-on-reflection*` should
be at the top of every file out of caution.

### Examples

```clojure
; avoid
(ns foo.bar)
(defn baz [a b] (+ a b))

; prefer
(ns foo.bar)
(set! *warn-on-reflection* true)
(defn baz [a b] (+ a b))
```

### Reference

* <http://clojure-goes-fast.com/blog/performance-nemesis-reflection>
