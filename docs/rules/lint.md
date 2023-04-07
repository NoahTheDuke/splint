# Lint

## lint/assoc-fn

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`assoc`-ing an update with the same key are hard to read. `update` is known and
idiomatic.

### Examples

```clojure
; bad
(assoc coll :a (+ (:a coll) 5))
(assoc coll :a (+ (coll :a) 5))
(assoc coll :a (+ (get coll :a) 5))

; good
(update coll :a + 5)
```

## lint/body-unquote-splicing

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 1.0           | 1.0             |

A macro that nests an `unquote-splicing` in a macro with a `& body` can lead
to subtle hard to debug errors. Better to wrap the `unquote-splicing` in
a `do` to force it into 'expression position'.

### Examples

```clojure
# bad
`(binding [max mymax] ~@body)

# good
`(binding [max mymax] (let [res# (do ~@body)] res#))
```

### Reference

* <https://blog.ambrosebs.com/2022/09/08/break-your-macros.html>

## lint/divide-by-one

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Checks for (/ x 1).

### Examples

```clojure
; bad
(/ x 1)

; good
x
```

## lint/dorun-map

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`run!` uses `reduce` which non-lazy.

### Examples

```clojure
; bad
(dorun (map println (range 10)))

; good
(run! println (range 10))
```

## lint/dot-class-method

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Using the `Obj/staticMethod` form maps the method call to Clojure's natural function
position.

### Examples

```clojure
; bad
(. Obj staticMethod args)

; good
(Obj/staticMethod args)
```

## lint/dot-obj-method

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Using the `.method` form maps the method call to Clojure's natural function position.

### Examples

```clojure
; bad
(. obj method args)

; good
(.method obj args)
```

## lint/duplicate-field-name

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.119       | 0.1.119         |

`deftype` and `defrecord` will throw errors if you define multiple fields
with the same name, but it's good to catch these things early too.

### Examples

```clojure
# bad
(defrecord Foo [a b a])

# good
(defrecord Foo [a b c])
```

### Reference

* [https://guide.clojure.style/#naming-conversion-functions]

## lint/fn-wrapper

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Avoid wrapping functions in pass-through anonymous function defitions.

### Examples

```clojure
; bad
(fn [num] (even? num))

; good
even?

; bad
(let [f (fn [num] (even? num))] ...)

; good
(let [f even?] ...)
```

### Reference

* [https://guide.clojure.style/#no-useless-anonymous-fns]

## lint/if-else-nil

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

### Examples

```clojure
; bad
(if (some-func) :a nil)

; good
(when (some-func) :a)
```

### Reference

* [https://guide.clojure.style/#when-instead-of-single-branch-if]

## lint/if-let-else-nil

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Idiomatic `if-let` defines both branches. `when-let` returns `nil` in the else branch.

### Examples

```clojure
; bad
(if-let [a 1] a nil)

; good
(when-let [a 1] a)
```

## lint/if-nil-else

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Idiomatic `if` defines both branches. `when-not` returns `nil` in the truthy branch.

### Examples

```clojure
; bad
(if (some-func) nil :a)

; good
(when-not (some-func) :a)
```

## lint/if-not-both

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`if-not` exists, so use it.

### Examples

```clojure
; bad
(if (not x) y z)

; good
(if-not x y z)
```

### Reference

* [https://guide.clojure.style/#if-not]

## lint/if-not-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`when-not` already defines an implicit `do`. Rely on it.

### Examples

```clojure
; bad
(if-not x (do (println :a) (println :b) :c))

; good
(if-not x (println :a) (println :b) :c)
```

## lint/if-not-not

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Two `not`s cancel each other out.

### Examples

```clojure
; bad
(if-not (not x) y z)

; good
(if x y z)
```

## lint/if-same-truthy

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`or` exists so use it lol.

### Examples

```clojure
; bad
(if x x y)

; good
(or x y)
```

## lint/into-literal

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`vec` and `set` are succinct and meaningful.

### Examples

```clojure
; bad
(into [] coll)

; good
(vec coll)

; bad
(into #{} coll)

; good
(set coll)
```

## lint/let-if

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

`if-let` exists so use it. Suggestions can be wrong as there's no code-walking to
determine if `result` binding is used in falsy branch.

### Examples

```clojure
; bad
(let [result (some-func)] (if result (do-stuff result) (other-stuff)))

; good
(if-let [result (some-func)] (do-stuff result) (other-stuff))
```

### Reference

* [https://guide.clojure.style/#if-let]

## lint/let-when

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

`when-let` exists so use it.

### Examples

```clojure
; bad
(let [result (some-func)] (when result (do-stuff result)))

; good
(when-let [result (some-func)] (do-stuff result))
```

### Reference

* [https://guide.clojure.style/#when-let]

## lint/loop-do

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`loop` has an implicit `do`. Use it.

### Examples

```clojure
; bad
(loop [] (do (println 1) (println 2)))

; good
(loop [] (println 1) (println 2))
```

## lint/loop-empty-when

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Empty loops with nested when can be `while`.

### Examples

```clojure
; bad
(loop [] (when (some-func) (println 1) (println 2) (recur)))

; good
(while (some-func) (println 1) (println 2) (recur))
```

## lint/missing-body-in-when

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.69          |

`when` calls should have at least 1 expression after the condition.

### Examples

```clojure
; bad
(when true)
(when (some-func))

; good
(when true (do-stuff))
(when (some-func) (do-stuff))
```

## lint/not-empty?

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 1.2.0           |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as
`(not (seq coll))` so it's best and fastest to use `seq` directly.

### Examples

```clojure
; bad
(not (empty? coll))

; good (chosen style :seq (default))
(seq coll)

; good (chosen style :not-empty)
(not-empty coll)
```

### Configurable Attributes

| Name         | Default | Options              |
| ------------ | ------- | -------------------- |
| Chosen Style | `:seq`  | `:seq`, `:not-empty` |

### Reference

* [https://guide.clojure.style/#nil-punning]

## lint/redundant-call

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

A number of core functions take any number of arguments and return the arg
if given only one. These calls are effectively no-ops, redundant, so they
should be avoided.

Current list of clojure.core functions this linter checks:

* `->`, `->>`
* `cond->`, `cond->>`
* `some->`, `some->>`
* `comp`, `partial`, `merge`

### Examples

```clojure
; bad
(-> x)
(->> x)
(cond-> x)
(cond->> x)
(some-> x)
(some->> x)
(comp x)
(partial x)
(merge x)

; good
x
```

## lint/take-repeatedly

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

`repeatedly` has an arity for limiting the number of repeats with `take`.

### Examples

```clojure
; bad
(take 5 (repeatedly (range 10))

; good
(repeatedly 5 (range 10))
```

## lint/thread-macro-one-arg

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1           | 0.1             |

Threading macros require more effort to understand so only use them with multiple
args to help with readability.

### Examples

```clojure
; bad
(-> x y)
(->> x y)

; good
(y x)

; bad
(-> x (y z))

; good
(y x z)

; bad
(->> x (y z))

; good
(y z x)
```

## lint/try-splicing

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 1.0           | 1.0             |

A macro that wraps a splicing unquote in a try-catch or try-finally can lead
to subtle hard to debug errors. Better to wrap the splicing unquote in a `do`
to force it into 'expression position'.

### Examples

```clojure
# bad
`(try ~@body (finally :true))

# good
`(try (do ~@body) (finally :true))
```

### Reference

* <https://blog.ambrosebs.com/2022/09/08/break-your-macros.html>
