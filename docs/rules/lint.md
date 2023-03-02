# Lint

## lint/apply-str

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for round-about clojure.string/reverse.

### Examples:
```clojure
; bad
(apply str x)

; good
(clojure.string/join x)
```

## lint/apply-str-interpose

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for round-about str/join.

### Examples:
```clojure
; bad
(apply str (interpose "," x))

; good
(clojure.string/join "," x)
```

## lint/apply-str-reverse

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for round-about clojure.string/reverse.

### Examples:
```clojure
; bad
(apply str (reverse x))

; good
(clojure.string/reverse x)
```

## lint/assoc-assoc

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Layering `assoc` calls are hard to read. `assoc-in` is known and idiomatic.

### Examples:
```clojure
; bad
(assoc coll :key1 (assoc (:key2 coll) :key2 new-val))
(assoc coll :key1 (assoc (coll :key2) :key2 new-val))
(assoc coll :key1 (assoc (get coll :key2) :key2 new-val))

; good
(assoc-in coll [:key1 :key2] new-val)
```

## lint/assoc-fn

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`assoc`-ing an update with the same key are hard to read. `update` is known and
idiomatic.

### Examples:
```clojure
; bad
(assoc coll :a (+ (:a coll) 5))
(assoc coll :a (+ (coll :a) 5))
(assoc coll :a (+ (get coll :a) 5))

; good
(update coll :a + 5)
```

## lint/assoc-in-one-arg

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

## lint/cond-else

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

It's nice when the default branch is consistent.

### Examples:
```clojure
; bad
(cond (< 10 num) (println 10) (< 5 num) (println 5) true (println 0))

; good
(cond (< 10 num) (println 10) (< 5 num) (println 5) :else (println 0))
```

## lint/conj-vector

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`vector` is succinct and meaningful.

### Examples:
```clojure
; bad
(conj [] :a b {:c 1})

; good
(vector :a b {:c 1})
```

## lint/divide-by-one

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for (/ x 1).

### Examples:
```clojure
; bad
(/ x 1)

; good
x
```

## lint/dorun-map

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`run!` uses `reduce` which non-lazy.

### Examples:
```clojure
; bad
(dorun (map println (range 10)))

; good
(run! println (range 10))
```

## lint/dot-class-method

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Using the `Obj/staticMethod` form maps the method call to Clojure's natural function
position.

; bad
(. Obj staticMethod args)

; good
(Obj/staticMethod args)

### Examples:


## lint/dot-obj-method

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Using the `.method` form maps the method call to Clojure's natural function position.

; bad
(. obj method args)

; good
(.method obj args)

### Examples:


## lint/eq-false

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`false?` exists so use it.

### Examples:
```clojure
; bad
(= false x)
(= x false)

; good
(false? x)
```

## lint/eq-nil

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`nil?` exists so use it.

### Examples:
```clojure
; bad
(= nil x)
(= x nil)

; good
(nil? x)
```

## lint/eq-true

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`true?` exists so use it.

### Examples:
```clojure
; bad
(= true x)
(= x true)

; good
(true? x)
```

## lint/eq-zero

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`zero?` exists so use it.

### Examples:
```clojure
; bad
(= 0 num)
(= num 0)
(== 0 num)
(== num 0)

; good
(zero? num)
```

## lint/filter-complement

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for (filter (complement pred) coll)

### Examples:
```clojure
; bad
(filter (complement even?) coll)

; good
(remove even? coll)
```

## lint/filter-vec-filter

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

filterv is preferable for using transients.

### Examples:
```clojure
; bad
(vec (filter pred coll))

; good
(filterv pred coll)
```

## lint/first-first

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

ffirst is succinct and meaningful.

### Examples:
```clojure
; bad
(first (first coll))

; good
(ffirst coll)
```

## lint/first-next

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

fnext is succinct and meaningful.

### Examples:
```clojure
; bad
(first (next coll))

; good
(fnext coll)
```

## lint/fn-wrapper

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Avoid wrapping functions in pass-through anonymous function defitions.

### Examples:
```clojure
; bad
(fn [num] (even? num))

; good
even?

; bad
(let [f (fn [num] (even? num))] ...)

; good
(let [f even?] ...)
```

## lint/if-else-nil

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Idiomatic `if` defines both branches. `when` returns `nil` in the else branch.

### Examples:
```clojure
; bad
(if (some-func) :a nil)

; good
(when (some-func) :a)
```

## lint/if-let-else-nil

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Idiomatic `if-let` defines both branches. `when-let` returns `nil` in the else branch.

### Examples:
```clojure
; bad
(if-let [a 1] a nil)

; good
(when-let [a 1] a)
```

## lint/if-nil-else

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Idiomatic `if` defines both branches. `when-not` returns `nil` in the truthy branch.

### Examples:
```clojure
; bad
(if (some-func) nil :a)

; good
(when-not (some-func) :a)
```

## lint/if-not-both

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`if-not` exists, so use it.

### Examples:
```clojure
; bad
(if (not x) y z)

; good
(if-not x y z)
```

## lint/if-not-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when-not` already defines an implicit `do`. Rely on it.

### Examples:
```clojure
; bad
(if-not x (do (println :a) (println :b) :c))

; good
(if-not x (println :a) (println :b) :c)
```

## lint/if-not-not

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Two `not`s cancel each other out.

### Examples:
```clojure
; bad
(if-not (not x) y z)

; good
(if x y z)
```

## lint/if-same-truthy

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`or` exists so use it lol.

### Examples:
```clojure
; bad
(if x x y)

; good
(or x y)
```

## lint/if-then-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Each branch of `if` can only have one expression, so using `do` to allow for multiple
expressions is better expressed with `when`.

### Examples:
```clojure
; bad
(if (some-func) (do (println 1) (println 2)))

; good
(when (some-func) (println 1) (println 2))
```

### Reference
* https://guide.clojure.style/#when-instead-of-single-branch-if

## lint/into-literal

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`vec` and `set` are succinct and meaningful.

### Examples:
```clojure
; bad
(into [] coll)

; good
(vec coll)

; bad
(into #{} coll)

; good
(set coll)
```

## lint/let-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`let` has an implicit `do`, so use it.

; bad
(let [a 1 b 2] (do (println a) (println b)))

; good
(let [a 1 b 2] (println a) (println b))

### Examples:


## lint/let-if

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`if-let` exists so use it. Suggestions can be wrong as there's no code-walking to
determine if `result` binding is used in falsy branch.

### Examples:
```clojure
; bad
(let [result (some-func)] (if result (do-stuff result) (other-stuff)))

; good
(if-let [result (some-func)] (do-stuff result) (other-stuff))
```

### Reference
* https://guide.clojure.style/#if-let

## lint/let-when

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when-let` exists so use it.

### Examples:
```clojure
; bad
(let [result (some-func)] (when result (do-stuff result)))

; good
(when-let [result (some-func)] (do-stuff result))
```

### Reference
* https://guide.clojure.style/#when-let

## lint/loop-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`loop` has an implicit `do`. Use it.

### Examples:
```clojure
; bad
(loop [] (do (println 1) (println 2)))

; good
(loop [] (println 1) (println 2))
```

## lint/loop-empty-when

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Empty loops with nested when can be `while`.

### Examples:
```clojure
; bad
(loop [] (when (some-func) (println 1) (println 2) (recur)))

; good
(while (some-func) (println 1) (println 2) (recur))
```

## lint/mapcat-apply-apply

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for (apply concat (apply map x y))

### Examples:
```clojure
; bad
(apply concat (apply map x y))

; good
(mapcat x y)
```

## lint/mapcat-concat-map

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Check for (apply concat (map x y z))

### Examples:
```clojure
; bad
(apply concat (map x y))
(apply concat (map x y z))

; good
(mapcat x y)
(mapcat x y z)
```

## lint/minus-one

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for simple -1 that should use `clojure.core/dec`.

### Examples:
```clojure
; bad
(- x 1)

; good
(dec x)
```

## lint/minus-zero

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for x - 0.

### Examples:
```clojure
; bad
(- x 0)

; good
x
```

## lint/missing-body-in-when

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when` calls should have at least 1 expression after the condition.

### Examples:
```clojure
; bad
(when true)
(when (some-func))

; good
(when true (do-stuff))
(when (some-func) (do-stuff))
```

## lint/multiply-by-one

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for (* x 1).

### Examples:
```clojure
; bad
(* x 1)
(* 1 x)

; good
x
```

## lint/multiply-by-zero

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for (* x 0).

### Examples:
```clojure
; bad
(* x 0)
(* 0 x)

; good
0
```

## lint/neg-checks

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`neg?` exists so use it.

### Examples:
```clojure
; bad
(< num 0)
(> 0 num)

; good
(neg? x)
```

## lint/nested-addition

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for simple nested additions.

### Examples:
```clojure
; bad
(+ x (+ y z))
(+ x (+ y z a))

; good
(+ x y z)
(+ x y z a)
```

## lint/nested-multiply

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for simple nested multiply.

### Examples:
```clojure
; bad
(* x (* y z))
(* x (* y z a))

; good
(* x y z)
(* x y z a)
```

## lint/next-first

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

nfirst is succinct and meaningful.

### Examples:
```clojure
; bad
(next (first coll))

; good
(nfirst coll)
```

## lint/next-next

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

nnext is succinct and meaningful.

### Examples:
```clojure
; bad
(next (next coll))

; good
(nnext coll)
```

## lint/not-empty?

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as
`(not (seq coll))` so it's best and fastest to use `seq` directly.

Examples

; bad
(not (empty? coll))

; good
(seq coll)

### Examples:


## lint/not-eq

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`not=` exists, so use it.

### Examples:
```clojure
; bad
(not (= num1 num2))

; good
(not= num1 num2)
```

## lint/not-nil?

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`some?` exists so use it.

### Examples:
```clojure
; bad
(not (nil? x))

; good
(some? x)
```

## lint/not-some-pred

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

not-any? is succinct and meaningful.

; bad
(not (some even? coll))

; good
(not-any? even? coll)

### Examples:


## lint/plus-one

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for simple +1 that should use `clojure.core/inc`.

### Examples:
```clojure
; bad
(+ x 1)
(+ 1 x)

; good
(inc x)
```

## lint/plus-zero

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Checks for x + 0.

### Examples:
```clojure
; bad
(+ x 0)
(+ 0 x)

; good
x
```

## lint/pos-checks

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`pos?` exists so use it.

### Examples:
```clojure
; bad
(< 0 num)
(> num 0)

; good
(pos? x)
```

## lint/take-repeatedly

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`repeatedly` has an arity for limiting the number of repeats with `take`.

### Examples:
```clojure
; bad
(take 5 (repeatedly (range 10))

; good
(repeatedly 5 (range 10))
```

## lint/thread-macro-no-arg

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Avoid wrapping vars in a threading macro.

### Examples:
```clojure
; bad
(-> x)
(->> x)

; good
x
```

## lint/thread-macro-one-arg

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Threading macros require more effort to understand so only use them with multiple
args to help with readability.

### Examples:
```clojure
; bad
(-> x y)
(->> x y)

; good
(y x)

; bad
(-> x (y))
(->> x (y))

; good
(y x)
```

## lint/tostring

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Convert (.toString) to (str)

### Examples:
```clojure
; bad
(.toString x)

; good
(str x)
```

## lint/update-in-assoc

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`update-in`-ing an `assoc` with the same key are hard to read. `assoc-in` is known
and idiomatic.

### Examples:
```clojure
; bad
(update-in coll [:a :b] assoc 5)

; good
(assoc-in coll [:a :b] 5)
```

## lint/update-in-one-arg

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`update-in` loops over the args, calling `update` for each key. If given a single key,
just call `update` directly instead for performance and readability improvements.

### Examples:
```clojure
; bad
(update-in coll [:k] inc)
(update-in coll [:k] + 1 2 3)

; good
(update coll :k inc)
(update coll :k + 1 2 3)
```

## lint/useless-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Examples:

; bad
(do coll)

; good
coll

### Examples:


## lint/when-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when` already defines an implicit `do`. Rely on it.

### Examples:
```clojure
; bad
(when x (do (println :a) (println :b) :c))

; good
(when x (println :a) (println :b) :c)
```

## lint/when-not-call

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when-not` exists so use it lol.

### Examples:
```clojure
; bad
(when (not x) :a :b :c)

; good
(when-not x :a :b :c)
```

## lint/when-not-do

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`when-not` already defines an implicit `do`. Rely on it.

### Examples:
```clojure
; bad
(when-not x (do (println :a) (println :b) :c))

; good
(when-not x (println :a) (println :b) :c)
```

## lint/when-not-empty?

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

`seq` returns `nil` when given an empty collection. `empty?` is implemented as
`(not (seq coll))` so it's best and fastest to use `seq` directly.

### Examples:
```clojure
; bad
(when-not (empty? ?x) &&. ?y)

; good
(when (seq ?x) &&. ?y)
```

## lint/when-not-not

| Enabled | Added |
| ------- | ----- |
|    true |   0.1 |

Two `not`s cancel each other out.

### Examples:
```clojure
; bad
(when-not (not x) y z)

; good
(when x y z)
```

## lint/with-meta-vary-meta

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
