# Patterns

The goal of the pattern DSL is ease of writing patterns. Because we can rely on Clojure's macro system, we don't have to deal with string-based patterns (such as Rubocop's otherwise excellent [Node Pattern]). Therefore, all of the examples in this page will be using raw Clojure code.

[Node Pattern]: https://docs.rubocop.org/rubocop-ast/node_pattern.html

## High-level description

The macro [`noahtheduke.splint.pattern/pattern`] builds the matching function. It takes a quoted form (either a single literal or a collection with arbitrarily nested data) and returns a function that will either match the given shape or return `nil`.

[`noahtheduke.splint.pattern/pattern`]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.pattern#pattern

## Types

Inside of a pattern, each element has roughly the same "type" as in Clojure, using [`noahtheduke.splint.pattern/simple-type`]. The table below shows an example of the input, the keyword returned by `simple-type`, the comparison function created by `pattern`, and any additional information.

[`noahtheduke.splint.pattern/simple-type`]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.pattern#simple-type

| Example | `simple-type` | Comparison function | Notes |
| --- | --- | --- | --- |
| `nil` | `:nil` | `(nil? form)` | |
| `true` / `false` | `:boolean` | `(identical? given form)` | |
| `\c` | `:char` | `(identical? given form)` | |
| `1` / `1N` / `1.0` / `0x1` | `:number` | `(= given form)` | Debatable whether to treat numbers as different. So far, patterns haven't needed both, but when they do we can split them up. |
| `:foo` / `:foo/bar` | `:keyword` | `(identical? given form)` | Because this isn't a string parsed by edamame, auto-resolved keywords must be handled in special cases. See below for more details. |
| `"foo"` | `:string` | `(.equals ^String given form)` | Interop is fastest. |
| `'foo` / `foo` | `:symbol` | `(= given form)` | Symbols don't support `identical?`. Some symbols are treated differently as part of the Pattern DSL. See below for more details. |
| `(:a 1)` | `:list` | `(seq? form)` | If there's a variable-length pattern, size is checked with `<=`. |
| `[:a 1]` | `:vector` | `(vector? form)` | If there's a variable-length pattern, size is checked with `<=`. |
| `{:a 1}` | `:map` | `(map? form)` | We only support matching on "simple" keys (no collections), so the size is checked with `<=`. |
| `#{:a 1}` | `:set` | `(set? form)` | |

## Special patterns

Some symbols are treated differently as part of the Pattern DSL. These are useful when writing more complex patterns.

Special patterns are in the shape of `(sym binding opts)`. Some have shorted forms. Any time there is a binding, if the given binding symbol doesn't start with a question mark, it is changed to have one: `(? x)` puts `?x` in the returned map from `pattern`.

* `?` is for single-value binding: `(? x pred?)` (short form `?x`). Matches anything and binds it to `?x`. If a predicate is given, it is called on the matched value and only binds when truthy.
* `?*` is for binding zero or more values in a sequence: `(?* x pred?)` (short form `?*x`). Matches any number of items and binds them to `?x`. If a predicate is given, it is called on each matched value with `(every? pred? items)` and only binds when that returns true.
* `?+` is the same as `?*` (short form `?+x`) but matches one or more items in the sequence.
* `??` is the same as `?*` (short form `??x`) but matches only zero or one items.
* `?|` binds a single value as `?`, but requires the third arg to be a vector of simple types, which it will try to match left-to-right: `(?| x [a b c])` will only match `a`, `b`, or `c`, but not `d`. Due to the required vector, there is no short form.

The binding `_` or short form `?_` are special: they match anything like a regular binding but don't create bindings in the map returned by `pattern`. They also don't unify with each other, so `[_ 1 _]` matches both `[0 1 0]` and `[0 1 2]`.

All predicates are resolved using `clojure.core/requiring-resolve`. It first tries to resolve with `clojure.core`, then with `noahtheduke.splint.rules.helpers`, then with the current namespace. If it can't resolve to a function, an `ExceptionInfo` is thrown.

**Note:** Any of the above can be treated as literals instead of DSL symbols by using the metadata `:splint/lit`: `(quote _)` -> `:any`, `(quote ^:splint/lit _)` -> `:symbol`.

## Maps

As of 1.0, maps in patterns cannot use complex keys: `'{(1 2 3) :true}`. This is due to the complexity of implementation. It's possible to implement it, but I haven't found a need yet and implementing collections for sets was already so much work I didn't feel like moving that logic over.

That said, I would _love_ to have this implemented, if anyone wants to take a swing at it. :eyebrows:
