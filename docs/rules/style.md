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
