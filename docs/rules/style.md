# Style

## style/cond-else

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

It's nice when the default branch is consistent.

### Examples:
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

## style/new-object

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`new` is discouraged for dot usage.

### Examples:
```clojure
; bad
(new java.util.ArrayList 100)

; good
(java.util.ArrayList. 100)
```

## style/prefer-boolean

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Use `boolean` if you must return `true` or `false` from an expression.

### Examples:
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

## style/prefer-clj-math

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Prefer clojure.math to interop.

### Examples:
```clojure
# bad
Math/PI
(Math/atan 45)

# good
clojure.math/PI
(clojure.math/atan 45)
```

## style/prefer-condp

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

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

### Examples:
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

## style/prefer-vary-meta

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`vary-meta` works like swap!, so no need to access and overwrite in two steps.

### Examples:
```clojure
; bad
(with-meta x (assoc (meta x) :filename filename))

; good
(vary-meta x assoc :filename filename)
```

## style/redundant-let

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Directly nested lets can be merged into a single let block.

### Examples:
```clojure
# bad
(let [a 1]
  (let [b 2]
    (println a b)))

(let [a 1
      b 2]
  (println a b))
```

## style/set-literal-as-fn

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Sets can be used as functions and they're converted to static items when
they contain constants, making them fairly fast. However, they're not as fast
as [[case]] and their meaning is less clear at first glance.

### Examples:
```clojure
# bad
(#{'a 'b 'c} elem)

# good
(case elem (a b c) elem nil)
```

## style/single-key-in

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`assoc-in` loops over the args, calling `assoc` for each key. If given a single key,
just call `assoc` directly instead for performance and readability improvements.

### Examples:
```clojure
; bad
(assoc-in coll [:k] 10)

; good
(assoc coll :k 10)
```
