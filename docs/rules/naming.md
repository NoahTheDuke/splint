# Naming

## naming/conversion-functions

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.94        | 0.1.94          |

Use `->` instead of `to` in the names of conversion functions.

### Examples

```clojure
# bad
(defn f-to-c ...)

# good
(defn f->c ...)
```

### Reference

* [https://guide.clojure.style/#naming-conversion-functions]

---

## naming/predicate

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 0.1.94          |

Functions that return a boolean should end in a question mark.

Doesn't verify the kind of function, just checks for anti-patterns in the
names. Also doesn't actually check the classic Common Lisp convention as we
have no way to know when a function name uses a word that naturally ends in
a 'p' (such as `map`).

### Examples

```clojure
# bad
(defn palindrome-p ...)
(defn is-palindrome ...)

# good
(defn palindrome? ...)
```

### Reference

* [https://guide.clojure.style/#naming-predicates]

---

## naming/record-name

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| true               | 0.1.69        | 1.0             |

Records should use PascalCase.

### Examples

```clojure
# bad
(defrecord foo [a b c])

# good
(defrecord Foo [a b c])
```

### Reference

* [https://guide.clojure.style/#naming-protocols-records-structs-and-types]
