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
