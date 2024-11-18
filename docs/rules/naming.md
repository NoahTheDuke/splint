# Naming

<!-- toc -->

- [naming/conventional-aliases](#namingconventional-aliases)
- [naming/conversion-functions](#namingconversion-functions)
- [naming/lisp-case](#naminglisp-case)
- [naming/predicate](#namingpredicate)
- [naming/record-name](#namingrecord-name)
- [naming/single-segment-namespace](#namingsingle-segment-namespace)

<!-- tocstop -->

## naming/conventional-aliases

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.3.0         | 1.3.0           |

Through community and core practices over the years, various core libraries have gained standard or expected aliases. To better align with the community, it's best to use those aliases in favor of alternatives.

Current namespaces and aliases:

| namespace | alias |
| --- | --- |
| clojure.core.async | async |
| clojure.core.matrix | mat |
| clojure.core.protocols | p |
| clojure.core.reducers | r |
| clojure.data.csv | csv |
| clojure.data.xml | xml |
| clojure.datafy | datafy |
| clojure.edn | edn |
| clojure.java.io | io |
| clojure.java.shell | sh |
| clojure.math | math |
| clojure.pprint | pp |
| clojure.set | set |
| clojure.spec.alpha | s |
| clojure.string | str |
| clojure.tools.cli | cli |
| clojure.tools.logging | log |
| clojure.walk | walk |
| clojure.zip | zip |

### Examples

```clojure
; avoid
(:require [clojure.string :as string])

; prefer
(:require [clojure.string :as str])
```

### Reference

* https://guide.clojure.style/#use-idiomatic-namespace-aliases

---

## naming/conversion-functions

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1.94        | 0.1.94          |

Use `->` instead of `to` in the names of conversion functions.

Will only warn when there is no `-` before the `-to-`.

### Safety
Uses simple string checking and can misunderstand English intention when `X-to-Y` isn't a conversion function.

### Examples

```clojure
; avoid
(defn f-to-c ...)

; prefer
(defn f->c ...)
(defn expect-f-to-c ...)
```

### Reference

* https://guide.clojure.style/#naming-conversion-functions

---

## naming/lisp-case

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 1.3.0         | 1.3.0           |

Use lisp-case for function and variable names. (Replacement is generated with [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).)

### Safety
Interop, json, and other styles can make it necessary to use such forms.

### Examples

```clojure
; avoid
(def someVar ...)
(def some_fun ...)

; prefer
(def some-var ...)
(defn some-fun ...)
```

### Reference

* https://guide.clojure.style/#naming-functions-and-variables

---

## naming/predicate

| Enabled by default | Safe  | Autocorrect | Version Added | Version Updated |
| ------------------ | ----- | ----------- | ------------- | --------------- |
| true               | false | false       | 0.1.69        | 0.1.94          |

Functions that return a boolean should end in a question mark.

### Safety
Doesn't verify the kind of function, just checks for anti-patterns in the names. Also doesn't actually check the classic Common Lisp convention as we have no way to know when a function name uses a word that naturally ends in a 'p' (such as `map`).

### Examples

```clojure
; avoid
(defn palindrome-p ...)
(defn is-palindrome ...)

; prefer
(defn palindrome? ...)
```

### Reference

* https://guide.clojure.style/#naming-predicates

---

## naming/record-name

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 0.1.69        | 1.15.2          |

Records should use PascalCase. (Replacement is generated with [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).)

### Examples

```clojure
; avoid
(defrecord foo [a b c])
(defrecord foo-bar [a b c])
(defrecord Foo-bar [a b c])

; prefer
(defrecord Foo [a b c])
(defrecord FooBar [a b c])
```

### Reference

* https://guide.clojure.style/#naming-protocols-records-structs-and-types

---

## naming/single-segment-namespace

| Enabled by default | Safe | Autocorrect | Version Added | Version Updated |
| ------------------ | ---- | ----------- | ------------- | --------------- |
| true               | true | false       | 1.3.0         | 1.3.0           |

Namespaces exist to disambiguate names. Using a single segment namespace puts you in direct conflict with everyone else using single segment namespaces, thus making it more likely you will conflict with another code base.

### Examples

```clojure
; avoid
(ns simple)

; prefer
(ns noahtheduke.simple)
```

### Reference

* https://guide.clojure.style/#no-single-segment-namespaces
