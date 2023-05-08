# Style

## style/apply-str

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for round-about clojure.string/reverse.

### Examples

```clojure
; bad
(apply str x)

; good
(clojure.string/join x)
```

---

## style/apply-str-interpose

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for round-about str/join.

### Examples

```clojure
; bad
(apply str (interpose "," x))

; good
(clojure.string/join "," x)
```

---

## style/apply-str-reverse

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for round-about clojure.string/reverse.

### Examples

```clojure
; bad
(apply str (reverse x))

; good
(clojure.string/reverse x)
```

---

## style/assoc-assoc

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Layering `assoc` calls are hard to read. `assoc-in` is known and idiomatic.

### Examples

```clojure
; bad
(assoc coll :key1 (assoc (:key2 coll) :key2 new-val))
(assoc coll :key1 (assoc (coll :key2) :key2 new-val))
(assoc coll :key1 (assoc (get coll :key2) :key2 new-val))

; good
(assoc-in coll [:key1 :key2] new-val)
```

---

## style/cond-else

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1.85          |

It's nice when the default branch is consistent.

### Examples

```clojure
; bad
(cond
  (< 10 num) (println 10)
  (< 5 num) (println 5)
  true (println 0))

; good
(cond
  (< 10 num) (println 10)
  (< 5 num) (println 5)
  :else (println 0))
```

### Reference

* https://guide.clojure.style/#else-keyword-in-cond

---

## style/conj-vector

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`vector` is succinct and meaningful.

### Examples

```clojure
; bad
(conj [] :a b {:c 1})

; good
(vector :a b {:c 1})
```

---

## style/def-fn

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 1.0           | 1.0             |

`(defn [])` is preferable over `(def (fn []))`. Extrapolate to closures.

### Examples

```clojure
# bad
(def check-inclusion
  (let [allowed #{:a :b :c}]
    (fn [i] (contains? allowed i))))

# good
(let [allowed #{:a :b :c}]
  (defn check-inclusion [i]
    (contains? allowed i)))

# bad
(def some-func
  (fn [i] (+ i 100)))

# good
(defn some-func [i]
  (+ i 100))
```

---

## style/eq-false

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`false?` exists so use it.

### Examples

```clojure
; bad
(= false x)
(= x false)

; good
(false? x)
```

---

## style/eq-nil

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`nil?` exists so use it.

### Examples

```clojure
; bad
(= nil x)
(= x nil)

; good
(nil? x)
```

---

## style/eq-true

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`true?` exists so use it.

### Examples

```clojure
; bad
(= true x)
(= x true)

; good
(true? x)
```

---

## style/eq-zero

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`zero?` exists so use it.

### Examples

```clojure
; bad
(= 0 num)
(= num 0)
(== 0 num)
(== num 0)

; good
(zero? num)
```

---

## style/filter-complement

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for (filter (complement pred) coll)

### Examples

```clojure
; bad
(filter (complement even?) coll)

; good
(remove even? coll)
```

---

## style/filter-vec-filterv

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

filterv is preferable for using transients.

### Examples

```clojure
; bad
(vec (filter pred coll))

; good
(filterv pred coll)
```

---

## style/first-first

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

ffirst is succinct and meaningful.

### Examples

```clojure
; bad
(first (first coll))

; good
(ffirst coll)
```

---

## style/first-next

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

fnext is succinct and meaningful.

### Examples

```clojure
; bad
(first (next coll))

; good
(fnext coll)
```

---

## style/let-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`let` has an implicit `do`, so use it.

### Examples

```clojure
; bad
(let [a 1 b 2] (do (println a) (println b)))

; good
(let [a 1 b 2] (println a) (println b))
```

---

## style/mapcat-apply-apply

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for (apply concat (apply map x y))

### Examples

```clojure
; bad
(apply concat (apply map x y))

; good
(mapcat x y)
```

---

## style/mapcat-concat-map

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Check for (apply concat (map x y z))

### Examples

```clojure
; bad
(apply concat (map x y))
(apply concat (map x y z))

; good
(mapcat x y)
(mapcat x y z)
```

---

## style/minus-one

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for simple -1 that should use `clojure.core/dec`.

### Examples

```clojure
; bad
(- x 1)

; good
(dec x)
```

---

## style/minus-zero

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for x - 0.

### Examples

```clojure
; bad
(- x 0)

; good
x
```

---

## style/multiple-arity-order

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 1.3.0         | 1.3.0           |

Sort the arities of a function from fewest to most arguments.

### Examples

```clojure
# bad
(defn foo
  ([x] (foo x 1))
  ([x y & more] (reduce foo (+ x y) more))
  ([x y] (+ x y)))

# good
(defn foo
  ([x] (foo x 1))
  ([x y] (+ x y))
  ([x y & more] (reduce foo (+ x y) more)))
```

### Reference

* https://guide.clojure.style/#multiple-arity-order

---

## style/multiply-by-one

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for (* x 1).

### Examples

```clojure
; bad
(* x 1)
(* 1 x)

; good
x
```

---

## style/multiply-by-zero

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for (* x 0).

### Examples

```clojure
; bad
(* x 0)
(* 0 x)

; good
0
```

---

## style/neg-checks

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`neg?` exists so use it.

### Examples

```clojure
; bad
(< num 0)
(> 0 num)

; good
(neg? num)
```

---

## style/nested-addition

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for simple nested additions.

### Examples

```clojure
; bad
(+ x (+ y z))
(+ x (+ y z a))

; good
(+ x y z)
(+ x y z a)
```

---

## style/nested-multiply

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for simple nested multiply.

### Examples

```clojure
; bad
(* x (* y z))
(* x (* y z a))

; good
(* x y z)
(* x y z a)
```

---

## style/new-object

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

`new` is discouraged for dot usage.

### Examples

```clojure
; bad
(new java.util.ArrayList 100)

; good
(java.util.ArrayList. 100)
```

---

## style/next-first

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

nfirst is succinct and meaningful.

### Examples

```clojure
; bad
(next (first coll))

; good
(nfirst coll)
```

---

## style/next-next

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

nnext is succinct and meaningful.

### Examples

```clojure
; bad
(next (next coll))

; good
(nnext coll)
```

---

## style/not-eq

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`not=` exists, so use it.

### Examples

```clojure
; bad
(not (= num1 num2))

; good
(not= num1 num2)
```

### Reference

* https://guide.clojure.style/#not-equal

---

## style/not-nil?

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`some?` exists so use it.

### Examples

```clojure
; bad
(not (nil? x))

; good
(some? x)
```

---

## style/not-some-pred

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

not-any? is succinct and meaningful.

### Examples

```clojure
; bad
(not (some even? coll))

; good
(not-any? even? coll)
```

---

## style/plus-one

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for simple +1 that should use `clojure.core/inc`.

### Examples

```clojure
; bad
(+ x 1)
(+ 1 x)

; good
(inc x)
```

---

## style/plus-zero

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Checks for x + 0.

### Examples

```clojure
; bad
(+ x 0)
(+ 0 x)

; good
x
```

---

## style/pos-checks

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`pos?` exists so use it.

### Examples

```clojure
; bad
(< 0 num)
(> num 0)

; good
(pos? num)
```

---

## style/prefer-boolean

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

Use `boolean` if you must return `true` or `false` from an expression.

### Examples

```clojure
# bad
(if some-val true false)
(if (some-func) true false)

# good
(boolean some-val)
(boolean (some-func))
```

### Reference

* https://guide.clojure.style/#converting-something-to-boolean

---

## style/prefer-clj-math

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

Prefer clojure.math to interop.

### Examples

```clojure
# bad
Math/PI
(Math/atan 45)

# good
clojure.math/PI
(clojure.math/atan 45)
```

### Reference

* https://guide.clojure.style/#prefer-clojure-math-over-interop

---

## style/prefer-condp

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 1.0             |

`cond` checking against the same value in every branch is a code smell.

This rule uses the first test-expr as the template to compare against each
other test-expr. It has a number of conditions it checks as it runs:

* The `cond` is well-formed (aka even number of args).
* The `cond` has more than 1 pair.
* The first test-expr is a list with 3 forms.
* The function of every test-expr must match the test-expr of the first
  test-expr.
  * The last test-expr isn't checked if it is `true` or a keyword.
* The last argument of every test-expr must match the last argument of the
  first test-expr.

Provided all of that is true, then the middle arguments of the test-exprs are
gathered and rendered into a `condp`.

### Examples

```clojure
# bad
(cond
  (= 1 x) :one
  (= 2 x) :two
  (= 3 x) :three
  (= 4 x) :four)

# good
(condp = x
  1 :one
  2 :two
  3 :three
  4 :four)

# bad
(cond
  (= 1 x) :one
  (= 2 x) :two
  (= 3 x) :three
  :else :big)

# good
(condp = x
  1 :one
  2 :two
  3 :three
  :big)
```

### Reference

* https://guide.clojure.style/#condp

---

## style/prefer-vary-meta

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1.69          |

`vary-meta` works like swap!, so no need to access and overwrite in two steps.

### Examples

```clojure
; bad
(with-meta x (assoc (meta x) :filename filename))

; good
(vary-meta x assoc :filename filename)
```

---

## style/redundant-let

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

Directly nested lets can be merged into a single let block.

### Examples

```clojure
# bad
(let [a 1]
  (let [b 2]
    (println a b)))

(let [a 1
      b 2]
  (println a b))
```

---

## style/set-literal-as-fn

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.119       | 1.0             |

Sets can be used as functions and they're converted to static items when
they contain constants, making them fairly fast. However, they're not as fast
as [[case]] and their meaning is less clear at first glance.

### Examples

```clojure
# bad
(#{'a 'b 'c} elem)

# good
(case elem (a b c) elem nil)
```

---

## style/single-key-in

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

`assoc-in` loops over the args, calling `assoc` for each key. If given a single key,
just call `assoc` directly instead for performance and readability improvements.

### Examples

```clojure
; bad
(assoc-in coll [:k] 10)

; good
(assoc coll :k 10)
```

---

## style/tostring

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Convert `(.toString)` to `(str)`.

### Examples

```clojure
; bad
(.toString x)

; good
(str x)
```

---

## style/update-in-assoc

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`update-in`-ing an `assoc` with the same key are hard to read. `assoc-in` is known
and idiomatic.

### Examples

```clojure
; bad
(update-in coll [:a :b] assoc 5)

; good
(assoc-in coll [:a :b] 5)
```

---

## style/useless-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 1.2.0           |

A single item in a `do` is a no-op. However, it is sometimes necessary to wrap expressions in `do`s to avoid issues, so `do` surrounding `~@something` will be skipped as well as `#(do something)`.

### Examples

```clojure
; bad
(do coll)

; good
coll

; skipped
(do ~@body)
#(do [%1 %2])
```

---

## style/when-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 1.2.0           |

`when` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; bad
(when x (do (println :a) (println :b) :c))

; good
(when x (println :a) (println :b) :c)
```

---

## style/when-not-call

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`when-not` exists so use it lol.

### Examples

```clojure
; bad
(when (not x) :a :b :c)

; good
(when-not x :a :b :c)
```

---

## style/when-not-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`when-not` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; bad
(when-not x (do (println :a) (println :b) :c))

; good
(when-not x (println :a) (println :b) :c)
```

---

## style/when-not-empty?

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1.69          |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as
`(not (seq coll))` so it's best and fastest to use `seq` directly.

### Examples

```clojure
; bad
(when-not (empty? ?x) &&. ?y)

; good
(when (seq ?x) &&. ?y)
```

---

## style/when-not-not

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

Two `not`s cancel each other out.

### Examples

```clojure
; bad
(when-not (not x) y z)

; good
(when x y z)
```
