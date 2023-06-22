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
| `(:a 1)` | `:list` | `(seq? form)` | If there's a `&&.` rest arg, size is checked with `<=`. |
| `[:a 1]` | `:vector` | `(vector? form)` | If there's a `&&.` rest arg, size is checked with `<=`. |
| `{:a 1}` | `:map` | `(map? form)` | We only support matching on "simple" keys (no collections), so the size is checked with `<=`. |
| `#{:a 1}` | `:set` | `(set? form)` | |

## Special symbols

Some symbols are treated differently as part of the Pattern DSL. These are useful when writing more complex patterns.

* `_` matches any value. Useful when a pattern only cares about a portion of the input and the replacement form will discard the rest.
* A symbol that starts with `%` is treated as a predicate. Example: `%keyword?` will match any keyword.
  * The `%` is removed and the resulting symbol is resolved using `clojure.core/requiring-resolve`. It first tries to resolve with `clojure.core`, then with `noahtheduke.splint.rules.helpers`, then with the current namespace. If it can't resolve to a function, an `ExceptionInfo` is thrown.
* A symbol that starts with `?` is treated as a binding. It matches any value and stores the value in the map returned by `pattern`.
* `&&.` is a pattern-specific rest argument. It requires that there is an immediately-following `?` binding symbol. Unlike normal bindings, multiple values of any kind are stored within this.
  * `&&.` works differently than Clojure's built-in `&` rest args and destructuring form. Whereas `& arg` must be in the final position, `&&. ?args` can have following entries, allowing patterns to match against items at the end of list or vector.
  * Example: `'(when ?test &&. ?exprs ?foo (recur))` matches against `(when true (+ 1 1) (recur))` as well as `(when true (+ 1 1) 2 3 4 (+ 5 5) (recur))`.

Predicates and bindings can be combined. The predicate must both start and end with a `%`, the predicate and binding must be separated by a dash `-`, and the binding must start with a `?`: `%keyword?%-?arg` matches any keyword and binds it to `?arg`.

**Note:** Any of the above can be treated as literals instead of DSL symbols by using the metadata `:splint/lit`: `(quote _)` -> `:any`, `(quote ^:splint/lit _)` -> `:symbol`.

**Note:** `:splint/lit` used to be `:spat/lit`. That still works but is deprecated and may stop working at some future date.

## Maps

As of 1.0, maps in patterns cannot use complex keys: `'{(1 2 3) :true}`. This is due to the complexity of implementation. It's possible to implement it, but I haven't found a need yet and implementing collections for sets was already so much work I didn't feel like moving that logic over.

That said, I would _love_ to have this implemented, if anyone wants to take a swing at it. :eyebrows:
