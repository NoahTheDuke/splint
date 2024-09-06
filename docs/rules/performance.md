# Performance

## performance/assoc-many

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.10.0        | 1.10.0          |

Assoc takes multiple pairs but relies on `seq` stepping. This is slower than
relying on multiple `assoc` invocations.

### Examples

```clojure
; avoid
(assoc m :k1 1 :k2 2 :k3 3)

; prefer
(-> m
    (assoc :k1 1)
    (assoc :k2 2)
    (assoc :k3 3))
```

---

## performance/avoid-satisfies

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | false       | 1.10.0        | 1.10.0          |

Avoid use of `satisfies?` as it is extremely slow. Restructure your code to rely on protocols or other polymorphic forms.

### Examples

```clojure
; avoid
(satisfies? Foo :bar)
```

### Reference

* <https://bsless.github.io/datahike-datalog-parser>

---

## performance/dot-equals

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.11          | 1.11            |

`=` is quite generalizable and built to handle immutable data. When using a literal, it can be significantly faster to use the underlying Java method.

Currently only checks string literals.

If `lint/prefer-method-values` is enabled, then the suggestion will use that syntax.

### Examples

```clojure
; avoid
(= "foo" s)

; prefer
(.equals "foo" s)
(String/equals "foo" s)
```

---

## performance/get-in-literals

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.10.0        | 1.10.0          |

`clojure.core/get-in` is both polymorphic and relies on seq stepping, which has heavy overhead when the listed slots are keyword literals. Faster to call them as functions.

### Examples

```clojure
; avoid
(get-in m [:some-key1 :some-key2 :some-key3])

; prefer
(-> m :some-key1 :some-key2 :some-key3)
```

---

## performance/get-keyword

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.10.0        | 1.10.0          |

`clojure.core/get` is polymorphic and overkill if accessing a map with a keyword literal. The fastest is to fall the map itself as a function but that requires a `nil` check, so the safest fast method is to use the keyword as function.

### Examples

```clojure
; avoid
(get m :some-key)

; prefer
(:some-key m)
```

---

## performance/into-transducer

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.11          | 1.11            |

`into` has a 3-arity and a 4-arity form. Both pour the given coll into the
new coll but when given a transducer in the 4-arity form, the transducer is
efficiently applied in between.

### Examples

```clojure
; avoid
(into [] (map inc (range 100)))

; prefer
(into [] (map inc) (range 100))
```

### Reference

* <https://bsless.github.io/code-smells>

---

## performance/single-literal-merge

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| false              | true | true        | 1.11          | 1.11            |

`clojure.core/merge` is inherently slow. Its major benefit is handling nil values. If there is only a single object to merge in and it's a map literal, that benefit is doubly unused. Better to directly assoc the values in.

**NOTE:** If the chosen style is `:single` and `performance/assoc-many` is enabled, the style will be treated as `:multiple` to make the warnings consistent.

### Examples

```clojure
; avoid
(merge m {:a 1 :b 2 :c 3})

; prefer (chosen style :single (default))
(assoc m :a 1 :b 2 :c 3)

; prefer (chosen style :multiple)
(-> m
    (assoc :a 1)
    (assoc :b 2)
    (assoc :c 3))
```

### Configurable Attributes

| Name            | Default   | Options                |
| --------------- | --------- | ---------------------- |
| `:chosen-style` | `:single` | `:single`, `:multiple` |
