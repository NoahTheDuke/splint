# Performance

## performance/assoc-many

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.10.0        | 1.10.0          |

Assoc takes multiple pairs but relies on `seq` stepping. This is slower than
relying on multiple `assoc` invocations.

### Examples

```clojure
# bad
(assoc m :k1 1 :k2 2 :k3 3)

# good
(-> m
    (assoc :k1 1)
    (assoc :k2 2)
    (assoc :k3 3))
```

---

## performance/avoid-satisfies

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.10.0        | 1.10.0          |

Avoid use of `satisfies?` as it is extremely slow. Restructure your code to rely on protocols or other polymorphic forms.

### Examples

```clojure
# bad
(satisfies? Foo :bar)
```

### Reference

* <https://bsless.github.io/datahike-datalog-parser>

---

## performance/get-in-literals

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.10.0        | 1.10.0          |

`clojure.core/get-in` is both polymorphic and relies on seq stepping, which has heavy overhead when the listed slots are keyword literals. Faster to call them as functions.

### Examples

```clojure
# bad
(get-in m [:some-key1 :some-key2 :some-key3])

# good
(-> m :some-key1 :some-key2 :some-key3)
```

---

## performance/get-keyword

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.10.0        | 1.10.0          |

`clojure.core/get` is polymorphic and overkill if accessing a map with a keyword literal. The fastest is to fall the map itself as a function but that requires a `nil` check, so the safest fast method is to use the keyword as function.

### Examples

```clojure
# bad
(get m :some-key)

# good
(:some-key m)
```
