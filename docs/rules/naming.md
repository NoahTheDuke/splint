# Naming

## naming/predicate

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Functions that return a boolean should end in a question mark.

Doesn't verify the kind of function, just checks for anti-patterns in the
names.

### Examples:
```clojure
# bad
(defn palindrome-p ...)
(defn is-palindrome ...)

# good
(defn palindrome? ...)
```

### Reference
* https://guide.clojure.style/#naming-predicates

## naming/record-name

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Records should use PascalCase.

### Examples:
```clojure
# bad
(defrecord foo [a b c])

# good
(defrecord Foo [a b c])
```

### Reference
* https://guide.clojure.style/#naming-protocols-records-structs-and-types
