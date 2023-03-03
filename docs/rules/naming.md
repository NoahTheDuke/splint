# Naming

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
