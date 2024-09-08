# Style

## style/apply-str

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for round-about `clojure.string/join`.

### Examples

```clojure
; avoid
(apply str x)

; prefer
(clojure.string/join x)
```

---

## style/apply-str-interpose

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for round-about `clojure.string/join`.

### Examples

```clojure
; avoid
(apply str (interpose "," x))

; prefer
(clojure.string/join "," x)
```

---

## style/apply-str-reverse

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for round-about `clojure.string/reverse`.

### Examples

```clojure
; avoid
(apply str (reverse x))

; prefer
(clojure.string/reverse x)
```

---

## style/assoc-assoc

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Layering `assoc` calls are hard to read. `assoc-in` is known and idiomatic.

### Examples

```clojure
; avoid
(assoc coll :key1 (assoc (:key2 coll) :key2 new-val))
(assoc coll :key1 (assoc (coll :key2) :key2 new-val))
(assoc coll :key1 (assoc (get coll :key2) :key2 new-val))

; prefer
(assoc-in coll [:key1 :key2] new-val)
```

---

## style/cond-else

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1.85          |

It's nice when the default branch is consistent.

### Examples

```clojure
; avoid
(cond
  (< 10 num) (println 10)
  (< 5 num) (println 5)
  true (println 0))

; prefer
(cond
  (< 10 num) (println 10)
  (< 5 num) (println 5)
  :else (println 0))
```

### Reference

* https://guide.clojure.style/#else-keyword-in-cond

---

## style/conj-vector

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`vector` is succinct and meaningful.

### Examples

```clojure
; avoid
(conj [] :a b {:c 1})

; prefer
(vector :a b {:c 1})
```

---

## style/def-fn

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.0           | 1.0             |

`(defn [])` is preferable over `(def (fn []))`. Extrapolate to closures too.

### Examples

```clojure
; avoid
(def check-inclusion
  (let [allowed #{:a :b :c}]
    (fn [i] (contains? allowed i))))

; prefer
(let [allowed #{:a :b :c}]
  (defn check-inclusion [i]
    (contains? allowed i)))

; avoid
(def some-func
  (fn [i] (+ i 100)))

; prefer
(defn some-func [i]
  (+ i 100))
```

---

## style/eq-false

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`false?` exists so use it.

### Examples

```clojure
; avoid
(= false x)
(= x false)

; prefer
(false? x)
```

---

## style/eq-nil

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`nil?` exists so use it.

### Examples

```clojure
; avoid
(= nil x)
(= x nil)

; prefer
(nil? x)
```

---

## style/eq-true

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`true?` exists so use it.

### Examples

```clojure
; avoid
(= true x)
(= x true)

; prefer
(true? x)
```

---

## style/eq-zero

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`zero?` exists so use it.

### Examples

```clojure
; avoid
(= 0 num)
(= num 0)
(== 0 num)
(== num 0)

; prefer
(zero? num)
```

---

## style/filter-complement

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for `(filter (complement pred) coll)`.

### Examples

```clojure
; avoid
(filter (complement even?) coll)

; prefer
(remove even? coll)
```

---

## style/filter-vec-filterv

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

filterv is preferable for using transients.

### Examples

```clojure
; avoid
(vec (filter pred coll))

; prefer
(filterv pred coll)
```

---

## style/first-first

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

ffirst is succinct and meaningful.

### Examples

```clojure
; avoid
(first (first coll))

; prefer
(ffirst coll)
```

---

## style/first-next

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`fnext` is succinct and meaningful.

### Examples

```clojure
; avoid
(first (next coll))

; prefer
(fnext coll)
```

---

## style/is-eq-order

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.15.0        | 1.15.0          |

`clojure.test/is` expects `=`-based assertions to put the expected value first.

This rule uses two checks on the `=` call to determine if it should issue a diagnostic:
* Is the first argument a symbol or a list with a symbol at the head? (A variable/local or a call.)
* Is the second argument a nil, boolean, char, number, keyword, or string?

### Examples

```clojure
; avoid
(is (= status 200))
(is (= (my-plus 1 2) 3))

; prefer
(is (= 200 status))
(is (= 3 (my-plus 1 2)))

; non-issues
(is (= (hash-map :a 1) {:a 1}))
(is (= (hash-set :a 1) #{:a 1}))
```

---

## style/let-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`let` has an implicit `do`, so use it.

### Examples

```clojure
; avoid
(let [a 1 b 2] (do (println a) (println b)))

; prefer
(let [a 1 b 2] (println a) (println b))
```

---

## style/mapcat-apply-apply

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for `(apply concat (apply map x y))`.

### Examples

```clojure
; avoid
(apply concat (apply map x y))

; prefer
(mapcat x y)
```

---

## style/mapcat-concat-map

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Check for `(apply concat (map x y z))`.

### Examples

```clojure
; avoid
(apply concat (map x y))
(apply concat (map x y z))

; prefer
(mapcat x y)
(mapcat x y z)
```

---

## style/minus-one

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for simple -1 that should use `clojure.core/dec`.

### Examples

```clojure
; avoid
(- x 1)

; prefer
(dec x)
```

---

## style/minus-zero

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for x - 0.

### Examples

```clojure
; avoid
(- x 0)

; prefer
x
```

---

## style/multiple-arity-order

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.3.0         | 1.3.0           |

Sort the arities of a function from fewest to most arguments.

### Examples

```clojure
; avoid
(defn foo
  ([x] (foo x 1))
  ([x y & more] (reduce foo (+ x y) more))
  ([x y] (+ x y)))

; prefer
(defn foo
  ([x] (foo x 1))
  ([x y] (+ x y))
  ([x y & more] (reduce foo (+ x y) more)))
```

### Reference

* https://guide.clojure.style/#multiple-arity-order

---

## style/multiply-by-one

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for (* x 1).

### Examples

```clojure
; avoid
(* x 1)
(* 1 x)

; prefer
x
```

---

## style/multiply-by-zero

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for (* x 0).

### Examples

```clojure
; avoid
(* x 0)
(* 0 x)

; prefer
0
```

---

## style/neg-checks

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`neg?` exists so use it.

### Examples

```clojure
; avoid
(< num 0)
(> 0 num)

; prefer
(neg? num)
```

---

## style/nested-addition

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for simple nested additions.

### Examples

```clojure
; avoid
(+ x (+ y z))
(+ x (+ y z a))

; prefer
(+ x y z)
(+ x y z a)
```

---

## style/nested-multiply

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for simple nested multiply.

### Examples

```clojure
; avoid
(* x (* y z))
(* x (* y z a))

; prefer
(* x y z)
(* x y z a)
```

---

## style/new-object

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 1.15.2          |

`new` special form is discouraged for dot usage.

**NOTE:** The style `:method-value` requires Clojure version 1.12+.

### Examples

```clojure
; avoid
(new java.util.ArrayList 100)

; prefer (chosen style :dot (default))
(java.util.ArrayList. 100)

; avoid (chosen style :method-value)
(java.util.ArrayList. 100)

; prefer (chosen style :method-value)
(java.util.ArrayList/new 100)
```

### Configurable Attributes

| Name            | Default | Options                 |
| --------------- | ------- | ----------------------- |
| `:chosen-style` | `:dot`  | `:dot`, `:method-value` |

---

## style/next-first

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`nfirst` is succinct and meaningful.

### Examples

```clojure
; avoid
(next (first coll))

; prefer
(nfirst coll)
```

---

## style/next-next

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`nnext` is succinct and meaningful.

### Examples

```clojure
; avoid
(next (next coll))

; prefer
(nnext coll)
```

---

## style/not-eq

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`not=` exists, so use it.

### Examples

```clojure
; avoid
(not (= num1 num2))

; prefer
(not= num1 num2)
```

### Reference

* https://guide.clojure.style/#not-equal

---

## style/not-nil?

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`some?` exists so use it.

### Examples

```clojure
; avoid
(not (nil? x))

; prefer
(some? x)
```

---

## style/not-some-pred

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

not-any? is succinct and meaningful.

### Examples

```clojure
; avoid
(not (some even? coll))

; prefer
(not-any? even? coll)
```

---

## style/plus-one

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for simple +1 that should use `clojure.core/inc`.

### Examples

```clojure
; avoid
(+ x 1)
(+ 1 x)

; prefer
(inc x)
```

---

## style/plus-zero

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Checks for x + 0.

### Examples

```clojure
; avoid
(+ x 0)
(+ 0 x)

; prefer
x
```

---

## style/pos-checks

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`pos?` exists so use it.

### Examples

```clojure
; avoid
(< 0 num)
(> num 0)

; prefer
(pos? num)
```

---

## style/prefer-boolean

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 0.1.69          |

Use `boolean` if you must return `true` or `false` from an expression.

### Examples

```clojure
; avoid
(if some-val true false)
(if (some-func) true false)

; prefer
(boolean some-val)
(boolean (some-func))
```

### Reference

* https://guide.clojure.style/#converting-something-to-boolean

---

## style/prefer-clj-math

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 0.1.69        | 0.1.69          |

**NOTE:** Requires Clojure version 1.11.0.

Prefer clojure.math to interop.

### Examples

```clojure
; avoid
Math/PI
(Math/atan 45)

; prefer
clojure.math/PI
(clojure.math/atan 45)
```

### Reference

* https://guide.clojure.style/#prefer-clojure-math-over-interop

---

## style/prefer-clj-string

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.9.0         | 1.9.0           |

Prefer clojure.string to interop.

| method | clojure.string |
| --- | --- |
| `.contains` | `clojure.string/includes?` |
| `.endsWith` | `clojure.string/ends-with?` |
| `.replace` | `clojure.string/replace` |
| `.split` | `clojure.string/split` |
| `.startsWith` | `clojure.string/starts-with?` |
| `.toLowerCase` | `clojure.string/lower-case` |
| `.toUpperCase` | `clojure.string/upper-case` |
| `.trim` | `clojure.string/trim` |

### Examples

```clojure
; avoid
(.toUpperCase "hello world")

; prefer
(clojure.string/upper-case "hello world")
```

### Reference

* https://guide.clojure.style/#prefer-clojure-string-over-interop

---

## style/prefer-condp

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1.69        | 1.0             |

`cond` checking against the same value in every branch is a code smell.

This rule uses the first test-expr as the template to compare against each other test-expr. It has a number of conditions it checks as it runs:

* The `cond` is well-formed (aka even number of args).
* The `cond` has more than 1 pair.
* The first test-expr is a list with 3 forms.
* The function of every test-expr must match the test-expr of the first test-expr.
* The last test-expr isn't checked if it is `true` or a keyword.
* The last argument of every test-expr must match the last argument of the first test-expr.

Provided all of that is true, then the middle arguments of the test-exprs are gathered and rendered into a `condp`.

### Safety
It's possible that the check isn't written correctly, so be wary of the suggested output.

### Examples

```clojure
; avoid
(cond
  (= 1 x) :one
  (= 2 x) :two
  (= 3 x) :three
  (= 4 x) :four)

; prefer
(condp = x
  1 :one
  2 :two
  3 :three
  4 :four)

; avoid
(cond
  (= 1 x) :one
  (= 2 x) :two
  (= 3 x) :three
  :else :big)

; prefer
(condp = x
  1 :one
  2 :two
  3 :three
  :big)
```

### Reference

* https://guide.clojure.style/#condp

---

## style/prefer-for-with-literals

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.15.0        | 1.17.0          |

The core builder functions are helpful when creating an object from an opaque sequence, but are much less readable when used in maps to get around issues with anonymous function syntax peculiarities.

### Examples

```clojure
; avoid
(map #(hash-map :a 1 :b %) (range 10))

; prefer
(for [item (range 10)] {:a 1 :b item})
```

---

## style/prefer-vary-meta

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1.69          |

`vary-meta` works like `swap!`, so no need to access and overwrite in two steps.

### Examples

```clojure
; avoid
(with-meta x (assoc (meta x) :filename filename))

; prefer
(vary-meta x assoc :filename filename)
```

---

## style/reduce-str

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.11          | 1.11            |

`reduce` calls the provided function on every element in the provided
collection. Because of how `str` is implemented, a new string is created
every time it's called. Better to rely on `clojure.string/join`'s efficient
StringBuilder and collection traversal.

Additionally, the 2-arity form of `reduce` returns the first item without
calling `str` on it if it only has one item total, which is
generally not what is expected when calling `str` on something.

### Examples

```clojure
; avoid
(reduce str x)
(reduce str "" x)

; prefer
(clojure.string/join x)
```

---

## style/redundant-let

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 0.1.69          |

Directly nested lets can be merged into a single let block.

### Examples

```clojure
; avoid
(let [a 1]
  (let [b 2]
    (println a b)))

(let [a 1
      b 2]
  (println a b))
```

---

## style/redundant-regex-constructor

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.10.0        | 1.10.0          |

Clojure regex literals (#"") are passed to `java.util.regex.Pattern/compile` at read time. `re-pattern` checks if the given arg is a Pattern, making it a no-op when given a regex literal.

### Examples

```clojure
; avoid
(re-pattern #".*")

; prefer
#".*"
```

---

## style/set-literal-as-fn

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 0.1.119       | 1.11            |

Sets can be used as functions and they're converted to static items when
they contain constants, making them fairly fast. However, they're not as fast
as [[case]] and their meaning is less clear at first glance.

### Examples

```clojure
; avoid
(#{'a 'b 'c} elem)

; prefer
(case elem (a b c) elem nil)
```

---

## style/single-key-in

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 0.1.69          |

`assoc-in` loops over the args, calling `assoc` for each key. If given a single key,
just call `assoc` directly instead for performance and readability improvements.

### Examples

```clojure
; avoid
(assoc-in coll [:k] 10)

; prefer
(assoc coll :k 10)
```

---

## style/tostring

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Convert `(.toString)` to `(str)`.

### Examples

```clojure
; avoid
(.toString x)

; prefer
(str x)
```

---

## style/trivial-for

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 1.11          | 1.11            |

`for` is a complex and weighty macro. When simply applying a function to each element, better to rely on other built-ins.

### Examples

```clojure
; avoid
(for [item items]
  (f item))

; prefer
(map f items)
```

### Reference

* <https://bsless.github.io/code-smells>

---

## style/update-in-assoc

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`update-in`-ing an `assoc` with the same key are hard to read. `assoc-in` is known
and idiomatic.

### Examples

```clojure
; avoid
(update-in coll [:a :b] assoc 5)

; prefer
(assoc-in coll [:a :b] 5)
```

---

## style/useless-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 1.2.0           |

A single item in a `do` is a no-op. However, it is sometimes necessary to wrap expressions in `do`s to avoid issues, so `do` surrounding `~@something` will be skipped as well as `#(do something)`.

### Examples

```clojure
; avoid
(do coll)

; prefer
coll

; skipped
(do ~@body)
#(do [%1 %2])
```

---

## style/when-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1.69        | 1.2.0           |

`when` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; avoid
(when x (do (println :a) (println :b) :c))

; prefer
(when x (println :a) (println :b) :c)
```

---

## style/when-not-call

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`when-not` exists so use it lol.

### Examples

```clojure
; avoid
(when (not x) :a :b :c)

; prefer
(when-not x :a :b :c)
```

---

## style/when-not-do

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

`when-not` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; avoid
(when-not x (do (println :a) (println :b) :c))

; prefer
(when-not x (println :a) (println :b) :c)
```

---

## style/when-not-empty?

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 0.1.69          |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as
`(not (seq coll))` so it's best and fastest to use `seq` directly.

### Examples

```clojure
; avoid
(when-not (empty? ?x) &&. ?y)

; prefer
(when (seq ?x) &&. ?y)
```

---

## style/when-not-not

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | true        | 0.1           | 1.2.0           |

Two `not`s cancel each other out.

### Examples

```clojure
; avoid
(when-not (not x) y z)

; prefer
(when x y z)
```
