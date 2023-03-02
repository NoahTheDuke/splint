# Style

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
