# Metrics

## metrics/fn-length

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.6.0         | 1.6.0           |

Avoid `defn`-defined functions longer than some number (10) of lines of code. Longer functions are harder to read and should be split into smaller-purpose functions that are composed.

The total length is configurable, and the size can be configured (`:body` or `:defn` styles) to be measured from the entire `defn` form or the vector+body.

### Examples

```clojure
;; :body style (default)
(defn foo
  [arg] ;; <- starts here
  0
  1
  ...
  9
  10) ;; <- ends here

(defn foo
  ([] (foo 100)) ;; <- starts and ends here
  ([arg] ;; <- starts here
   0
   1
   ...
   9
   10)) ;; <- ends here

;; :defn style
(defn foo ;; <- starts here
  [arg]
  0
  1
  ...
  9
  10) ;; <- ends here

(defn foo ;; <- starts here
  ([] (foo 100))
  ([arg]
   0
   1
   ...
   9
   10)
) ;; <- ends here
```

### Configurable Attributes

| Name            | Default | Options          |
| --------------- | ------- | ---------------- |
| `:chosen-style` | `:body` | `:body`, `:defn` |
| `:length`       | `10`    | Number           |

### Reference

* https://guide.clojure.style/#function-length

---

## metrics/parameter-count

| Enabled by default | Version Added | Version Updated |
| ------------------ | ------------- | --------------- |
| false              | 1.7.0         | 1.7.0           |

Avoid parameter lists with more than 4 positional parameters.

The number of parameters can be configured with `:count`. The default style `:positional` excludes `& args` rest parameters, and the style `:include-rest` includes them.

Functions with multiple arities will have each arity checked.

### Examples

```clojure
;; :positional style (default)
; bad
(defn example [a b c d e] ...)
(defn example ([a b c d e] ...) ([a b c d e f g] ...))
(defn example [a b c d e & args] ...)

; good
(defn example [a b c d] ...)
(defn example ([a b c] ...) ([a b c e] ...))
(defn example [a b c d & args] ...)

;; :include-rest style
; bad
(defn example [a b c d & args] ...)

; good
(defn example [a b c & args] ...)
```

### Configurable Attributes

| Name            | Default       | Options                        |
| --------------- | ------------- | ------------------------------ |
| `:chosen-style` | `:positional` | `:positional`, `:include-rest` |
| `:count`        | `4`           | Number                         |

### Reference

* https://guide.clojure.style/#function-positional-parameter-limit
