# Change Log

This changelog is loose. Versions are not semantic, they are incremental. Splint is not meant to be infrastructure, so don't rely on it like infrastructure; it is a helpful development tool.

## Unreleased

### New Rules

- `lint/update-with-swap`: Prefer `(swap! (:counter state) + 5)` over `(update state :counter swap! + 5)`. (See [#30](https://github.com/NoahTheDuke/splint/issues/30).)

### Changed

Update rules:

- `lint/existing-constant`: narrow to only `clj`, and expand it to cover clojure 1.10 (by using `java.lang.Math/PI` and `java.lang.Math/E`).
- `lint/incorrectly-swapped`: look for destructuring with identical binding and exprs: `(let [[a b] [a b]] ...)`.
- `lint/no-catch`: require at least one form in `try` call.
- `lint/prefer-method-values`: simplify docs, examples, remove `:autocomplete` as it uses a dummy class.
- `lint/redundant-call`: add configurable fn list with `:fn-names`.
- `lint/require-explicit-param-tags`: clean up docs.
- `performance/into-transducer`: remove `cat` as it can't be used in the incorrect form.
- `performance/into-transducer`: add configurable fn list with `:fn-0-arg` and `:fn-1-arg` (depending on how many arguments the fn accepts).

Others:

- Bump `edamame` to `1.4.31` to support `#^` metadata and no-op reader conditionals.
- Move `resources/SPLINT_VERSION` and `resources/config/default.edn` to `resources/noahtheduke/splint/SPLINT_VERSION` and `resources/noahtheduke/splint/config/default.edn`, respectively.

### Fixed

- `lint/rand-int-one` only checks for numbers between `-1` and `1` (inclusive). (See [#31](https://github.com/NoahTheDuke/splint/issues/31).)

## 1.21.0 - 2025-06-24

### New Rules

- `style/prefixed-libspecs`: Prefer flat `require` libspecs to prefixed/nested libspecs: `[clojure.string :as str] [clojure.set :as set]` over `[clojure [string :as str] [set :as set]]`. Currently does not support suggesting alternatives.
- `lint/rand-int-one`: Calls to `(rand-int 1)` always return `0`, so this is likely an error.
- `lint/no-catch`: Require `(try)` calls to have at least 1 `catch` (or `finally`) clause. Supports two styles: `:accept-finally` and `:only-catch`. `:accept-finally` will count a `finally` clause and not raise a warning, while `:only-catch` requires all `try` calls to have a `catch` clause.
- `lint/catch-throwable`: Prefer specific Exceptions and Errors over `(catch Throwable t ...)`. Has `:throwables []` config, which can be used to specify any particular Throwables to disallow.
- `lint/identical-branches`: Checks for identical branches of `if` and `cond` forms: `(if (pred) foo foo)`, `(cond (pred1) foo (pred2) foo)`. In `cond` branches, only checks consecutive branches as order of checks might be important otherwise.
- `lint/no-op-assignment`: Avoid writing `(let [foo foo] ...)` or similar. No need to assign a variable to itself. Skips when the expr is in a reader conditional or has a type-hint.
- `lint/min-max`: When clamping a number with `min` and `max`, enforce that `min` has the higher number.
- `lint/existing-constant`: Check defs for numbers that look like existing constants: `(def pi 3.14)` should be `clojure.math/PI`.

### Added

- New config option for `lint/fn-wrapper`: `:names-to-skip`. Given that many macros require wrapping, skipping them (or any other calls) can be configured with `:names-to-skip`, which takes a vector of simple symbols to skip during analysis. For example, `lint/fn-wrapper {:names-to-skip [inspect]}` will not trigger on `(add-tap (fn [x] (morse/inspect)))`.
- New config option for `style/redundant-nested-call`: `:fn-names`. By default, `style/redundant-nested-call` only checks a handful of `clojure.core` vars. To check against custom functions, add them to the config with `style/redundant-nested-call {:fn-names [foo bar]}`.
- Rules support `:config-coercer`, a one-arg function that takes the final result of a rules' processed config and should return it. Allows for rules to define custom ways of handling config data before it's used (such as unifying args). See `lint/catch-throwable` for an example.
- DEV ONLY: add roughly 10 new integrations tests for github repos that already rely on Splint.

### Changed

- Disable `lint/thread-macro-one-arg` by default. It harms readability in a lot of cases and has limited usefulness.
- `:import` parsing now includes both the base class name as well as the fully qualified class name in the returned map, which improves all interop scenarios.
- Include the misplaced type hint in the `Form` output of `lint/misplaced-type-hint`.
- For `naming/conversion-functions`, skip functions that have multi-segment tails as well. For example, no longer triggers on `x-to-special-y`.

### Fixed

- Correctly resolve auto-resolving keywords in the current namespace: `(ns foo) ::bar` will be parsed as `:foo/bar` instead of `:splint-auto-current/bar`.
- `naming/lisp-case`: Requires name to be a symbol. Skips names that contain `->` or end in `?`, which indicate conversion functions or type predicates, respectfully..
- `naming/record-name`: Doesn't crash when analyzing `` `(defrecord ~@body)``.
- `style/prefer-for-with-literals`: Skips when the arg is a destructuring binding.

## 1.20.0 - 2025-03-28

### New Rules

- `lint/misplaced-type-hint`: Prefer `(defn make-str ^String [] "abc")` over `(defn ^String make-str [] "abc")`. Only checks `defn` forms at the moment.

### Changed

- Support Clojure 1.10, don't force downstream users to update.
- Add matrix testing for each supported clojure version to GHA.
- Update `edamame` to 1.4.28.

## 1.19.0 - 2024-11-26

### New Rules

- `style/redundant-nested-call`: Prefer `(+ 1 2 3 4)` to `(+ 1 2 (+ 3 4))`.

### Added

- Table of Contents to each of the rules pages.

### Fixed

- `style/into-literal` ignores when in a threaded context.
- `lint/def-fn` ignores when in a syntax-quoted context.

## 1.18.0 - 2024-10-03

### New Rules

- `lint/defmethod-names`: Require that `defmethod` calls define a name for the function body. This helps improve stack traces. Suggested names are built from the dispatch value and cannot be trusted to be unique or usable, so while the rule is safe, it does not support autocorrect. Disabled by default.

### Fixed

- `style/is-eq-order` relaxed expected input to accept any non-quoted list. (See [#25](https://github.com/NoahTheDuke/splint/issues/25).)

## 1.17.1 - 2024-09-20

### Fixed

- Autocorrect skips quoted forms.
- Autocorrect only saves non-empty files.
- Autocorrect removes all rules that don't have `:autocorrect`.
- `--interactive` has been added to the CLI options, making it usable. (oops)
- `--interactive` now applies `:autocorrect` so it doesn't need to be specified.

## 1.17.0 - 2024-09-08

**Big feature:** Safety and Autocorrection

Every rule has been marked as safe or unsafe. Safe rules don't generate false positives and any suggested alternatives can be used directly. Unsafe rules may generate false positives or their suggested alternatives may contain errors.

Rules that are safe may also perform autocorrection, which is tracked in `defrule` with `:autocorrect`. Rules may only perform autocorrection if they're safe.

The [Rules Overview](docs/rules-overview.md) has been expanded as well.

### Changed

- Update dependencies. `edamame` 1.4.27 supports the Clojure 1.12 array syntax: `Integer/1`.
- Rules documentation can now handle 3 different directives: `@note`, `@safety`, and `@examples`. All existing `# Examples` have been converted to `@examples`, and the relevant rule docstrings have been updated.
- Rules documentation generation has been changed to handle the above.
- Added `:autocorrect` to `defrule`, `:safe` to config schema.

### Fixed

- `lint/redundant-str-call` ignores when used in threading macros. (See [#20](https://github.com/NoahTheDuke/splint/issues/20).)
- `lint/redundant-call` ignores when used in threading macros. (See [#21](https://github.com/NoahTheDuke/splint/issues/21).)
- Fix matching `nil` when input is too short in patterns, which fixes subtle issues with `lint/cond-else`.
- Make `support-clojure-version?` only compare minor versions if major version numbers match, and likewise with incremental/minor version numbers.

## 1.16.0 - 2024-08-08

### New rules

- `lint/redundant-str-call`: Don't call `str` on input that's guaranteed to be a string: Prefer `"foo"` to `(str "foo")`, `(str "foo" bar)` to `(str (str "foo" bar))`, and `(format "foo%s" bar)` to `(str (format "foo%s" bar))`. (See [clj-kondo#2323](https://github.com/clj-kondo/clj-kondo/issues/2323) for inspiration.)
- `lint/duplicate-case-test`: Don't use the same case test constant more than once.
- `lint/locking-object`: Prefer to lock on a symbol bound to `(Object.)`.

### Added

- `--only RULE` cli flag to run only specified rules or genres. Can be used multiple times. ([#13](https://github.com/NoahTheDuke/splint/issues/13))

### Changed

- Switched from `clojure.pprint` to [fipp](https://github.com/brandonbloom/fipp) for pretty-printing code. Fast and easy to extend.
- Use [org.flatland/ordered](https://github.com/clj-commons/ordered) (when run in Clojure) to keep parsed maps and sets in their read order.
- Add exceptions to diagnostics and print stack traces in all errors. Should fix bugs where all that's printed is `Splint encountered an error: ""` which is unhelpful and shameful.
- External links in `default.edn` are now `:links`, a vector of strings. This allows for listing multiple references.
- Switch all tests to Lazytest to do some dogfooding.
- Enforce that `??` only 1 or 2 arguments, and if provided, that the predicate is a symbol.
- Updated all dependencies.
- Switch tests to use [Lazytest](https://github.com/NoahTheDuke/lazytest).

### Fixed

- Outputs `json` and `json-pretty` now work with Babashka, by relying on Babashka's built-in `chehire.core` instead of `clojure.data.json`. This shouldn't result in any observable differences. I'd use `cheshire.core` for both, but `cheshire.core` is much bigger and more complicated than `clojure.data.json`, and it's a pain in the ass imo.

## 1.15.2 - 2024-05-09

### Changed

- Expanded documentation, added CONTRIBUTING.md.
- Switch all existing uses of `deftest` (including in `new_rule.tmpl`) back to using `defexpect`. Sean fixed the 3-arg issue when I raised it in <https://github.com/clojure-expectations/clojure-test/issues/35>, and it's nice to only import a single namespace instead of multiple.
- Add table of namespaces to aliases in `naming/conventional-aliases` docs.
- `naming/record-name` now uses `camel-snake-kebab` to check and convert the given record name to PascalCase.
- Add `:method-value` style to `style/new-object` to suggest `Foo/new` instead of `Foo.`.
- Disable `lint/dot-class-method` and `lint/dot-obj-method` when `lint/prefer-method-values` is enabled.
- Track rules on ctx instead of passing as a separate argument in `runner` functions.
- Move rules from `(:config ctx)` to `(:rules ctx)` as map of rule-name to rule map. Add `(:rules-by-type ctx)`, a map of simple-type to vector of rule names. Change `check-all-rules-of-type` to reduce over rule names and pull the rule map from `ctx`.

### Fixed

- Remove incorrect guide-ref in `lint/duplicate-field-name`.
- Get auto-gen-config working again. (See [#16](https://github.com/NoahTheDuke/splint/issues/16))

## 1.15.1 - 2024-05-06

### Changed

- Updated all rules examples to use "avoid" and "prefer" instead of "bad" and "good". This aligns closer with Splint's perspective on the issues found.
- Updated configuration docs to be more explicit about enabling and disabling rules and the use of `global`. (See [#11](https://github.com/NoahTheDuke/splint/issues/11) and [#12](https://github.com/NoahTheDuke/splint/issues/12))

### Fixed

- False positive in `lint/assoc-fn` when `f` is a macro. Covered `or` explicitly, no good generalized solution at the moment. (See [#15](https://github.com/NoahTheDuke/splint/issues/15).)
- `--print-config` properly includes the genre of printed rules.

## 1.15.0 - 2024-05-01

### New Rules

- `style/is-eq-order`: Prefer `(is (= 200 status))` over `(is (= status 200))` when writing assertions.
- `style/prefer-for-with-literals`: Prefer `(for [item coll] {:a 1 :b item})` over `(map #(hash-map :a 1 :b %) coll)`. (See [#10](https://github.com/NoahTheDuke/splint/issues/10).)

### Added

- `-r`/`--require` cli flag that can be used multiple times and `require` top-level config option that takes a vector of strings. These are loaded with `load-file` at run-time to allow for custom rules to be written and used. (See [#8](https://github.com/NoahTheDuke/splint/issues/8).) This is inherently unsafe, so don't run code you don't know.

### Changed

- Slight change to the patterns, now a final-position `?*` or `?+` will immediately return the rest of the current input instead of accumulating it one-by-one.
- Reformatted every file to use [Tonsky's Better Clojure formatting](https://tonsky.me/blog/clojurefmt/).
- `lint/warn-on-reflection` now checks that the file contains a proper `ns` form before issuing a diagnostic.
- Updated README speed comparison chart.

## v1.14.0 - 2024-02-19

### Changed

- General performance increases in rules:

  - `lint/body-unquote-splicing`
  - `lint/if-else-nil`
  - `lint/underscore-in-namespace`
  - `lint/warn-on-reflection`
  - `metrics/parameter-count`
  - `naming/conversion-function`
  - `naming/predicate`
  - `naming/record-name`
  - `naming/single-segment-namespace`
  - `style/def-fn`
  - `style/eq-zero`
  - `style/prefer-clj-string`
  - `style/prefer-condp`
  - `style/reduce-str`
  - `style/single-key-in`
  - `style/tostring`
  - `style/useless-do`

- Remove documentation about `?_` short form, as it's covered by the existing `?` and `_` binding rules.
- Expand `?foo` short-forms in patterns to their `(? foo)` special form. Simplifies matching functions, makes the pattern DSL more consistent. Now `?|foo` will throw immediately instead of part-way through macroexpansion.
- Updated pattern docs with a small example at the top.
- Simplified `?|` matcher logic to use a set, as that's faster than creating multiple `read-form` patterns in a let block and checking each one.

### Fixed

- Correctly suggest `Obj/staticMethod` when given `(. Obj (staticMethod))` in `lint/dot-class-usage`.
- Only suggest `naming/conversion-functions` when there's no `-` in the part before `-to-`. (Will warn on `f-to-g`, will not warn on `expect-f-to-c`.)
- Correctly render args in `lint/assoc-fn`.

## v1.13 - 2024-02-14

### New Rules

- `lint/prefer-method-values`: Prefer `(^[] String/toUpperCase "noah")` to `(.toUpperCase "noah")`.
- `lint/require-explicit-param-tags`: Prefer `(^[File] File/mkdir (io/file \"a\"))` to `(File/mkdir (io/file \"a\"))`. Prefer `(^[String String] File/createTempFile \"abc\" \"b\")` to `(^[_ _] File/createTempFile \"abc\" \"b\")`. Has `:missing`, `:wildcard`, and `:both` styles, which check for lack of any `:param-tags`, usage of `_` in a `:param-tags`, and both. Defaults to `:wildcard`.

### Changed

- Add support for `lint/prefer-method-values` in `performance/dot-equals`.
- Switch `new_rule.tmpl` to use `deftest`. `defexpect` is a thin wrapper and has the annoying "if given two non-expect entries, wrap in expect", which doesn't work when we use custom expect macros.

## v1.12 - 2024-02-09

### Added

- `re-find:` and `string:` syntaxes for path `:excludes`. `re-find` uses `clojure.core/re-find`, so the regex doesn't have to match the entire file path, just any portion. `string` uses `clojure.string/includes?`, so a fixed string anywhere in the file path.

### Changed

- Updated [edamame][edamame] to v1.4.25 in support of the new Clojure 1.12 `^[]`/`:param-tags` feature.

## v1.11 - 2023-12-11

### New Rules

- `lint/underscore-in-namespace`: Prefer `(ns foo-bar)` to `(ns foo_bar)`.
- `performance/dot-equals`: Prefer `(.equals "foo" bar)` to `(= "foo" bar)`. Only cares about string literals right now.
- `performance/single-literal-merge`: Prefer `(assoc m :a 1 :b 2)` to `(merge m {:a 1 :b 2})`. Has `:single` and `:multiple` styles, either a single `assoc` call or threaded multiple calls.
- `performance/into-transducer`: Prefer `(into [] (map f) coll)` to `(into [] (map f coll))`.
- `style/trivial-for`: Prefer `(map f items)` over `(for [item items] (f item))`.
- `style/reduce-str`: Prefer `(clojure.string/join coll)` over `(reduce str coll)`.

### Added

- `global` top-level `.splint.edn` config that applies to all rules.
- Support `:excludes` in both `global` and rules-specific configs. Accepts a vector of [java.nio.file.FileSystem/getPathMatcher](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-) globs or regexes. When in `global`, matching files are removed from being processed at all. When in a specific rule, the rule is disabled before matching files are checked.

### Changed

- Added `min`, `max`, and `distinct?` to `lint/redundant-call`.
- Change `style/set-literal-as-fn` default to `false`. It's not idiomatic and I don't know that it's any faster either.

### Fixed

- Project file now accepts all reader macros.
- `--auto-gen-config`, adds test.

## v1.10.1 - 2023-07-25

### Fixed

- `performance/assoc-many` should only trigger when there are more than 1 pair.

## v1.10.0 - 2023-07-25

The big feature here is adding support to run `splint` without specifying paths. Now Splint will read the `deps.edn` or `project.clj` file in the current directory and check the paths in the base definition as well as `:dev` and `:test` aliases/profiles if no path argument is given. Splint still doesn't support specifying paths in `.splint.edn`, nor does it allow for using paths from a project file as well as additional paths when called, but those are planned.

The second big change is moving from the old DSL to the new `pangloss/pattern` inspired DSL. More flexible, more usable, overall better.

The third is adding `performance` rules. These are off by default and are designed for those who want to pinch their cycles. Some affect common code (`get-in`) and some are much more rare (`satisfies`), but they're all designed to help you be mindful of slower paths.

### Breaking

- Moved `spat.parser` to `splint.parser`.
- Moved `spat.pattern` to `splint.pattern`. RIP `spat`, you treated me well for 9 months, but keeping `spat` and `splint` separate is no longer helpful.
- Switched to the new pattern system, updated all rules.

### New Rules

- `performance/assoc-many`: Prefer `(-> m (assoc :k1 1) (assoc :k2 2))` over `(assoc m :k1 1 :k2 2)`.
- `performance/avoid-satisfies`: Do not use `clojure.core/satisfies?`, full stop.
- `performance/get-in-literals`: Prefer `(-> m :k1 :k2 :k3)` over `(get-in m [:k1 :k2 :k3])`.
- `performance/get-keyword`: Prefer `(:k m)` over `(get m :k)`.
- `style/redundant-regex-constructor`: Prefer `#"abc"` over `(re-pattern #"abc")`.

### Added

- Implemented faster/more efficient versions of Clojure standard library functions:
  - `->list`: concrete list building instead of apply . Useful anywhere a lazy-seq might be returned otherwise. seq/vec input: 40/43 us -> 28/15 us
  - `mapv*`: `mapv` but short-circuits empty input and uses `object-array`. Still unsure of this one. 36 us -> 36 us
  - `run!*`: `run!` but short-circuits empty input and uses `.iterator` to perform the side-effects. Does not support `reduced`. 7 us -> 950 ns
  - `pmap*`: Avoids lazy-seq overhead and relies on Java's built-in Executors. 3.34 s -> 202 ms
  - `walk*` and `postwalk*`: Primarily useful in `replace`, but may prove useful otherwise. Only supports `simple-type` defined types. 72 us -> 25 us
- `splint.config/read-project-file` returns a map of `:clojure-version` and `:paths`, taken from the project file (`deps.edn` or `project.clj`) in the current directory. If no file is found, `:paths` is `nil` and `:clojure-version` is pulled from `*clojure-version*`.
- `:min-clojure-version` in `defrule`, allowing for rules to specify the minimum version of clojure they require. Rules that are below the supported version are disabled at preparation time and can't be enabled during a run. Acceptable shape is a map of at least one of `:major`, `:minor`, and `:incremental`.
  - Include this in rule documentation.
- `test-helpers/with-temp-file` and `test-helpers/print-to-file!` to test file contents.

### Changed

- Move `spat.parser/parse-string` and `spat.parser/parse-string-all` into the test-helper namespace, and replace with `parse-file` which accepts the `file-obj` map.
- Parse data reader/tagged literals as maps instead of lists, and put the extension (dialect) into the symbol's metadata.
- Defer building cli summary until needed.
- Use new `splint.config/slurp-edn` to read config files, parsed with [edamame][edamame].
- Changed `:spat/lit` metadata to `:splint/lit`. `:spat/lit` still works for the time being, but no promises.
- `splint.printer/print-results` now accepts only the `results` object, which should additionally have `:checked-files` and `:total-time`.
- Output formats `simple`, `full,` and `clj-kondo` now print the number of files checked as well: `"Linting took 1ms, checked 3 files, 3 style warnings"`
- Moved `splint.replace/revert-splint-reader-macros` into `splint.printer` where it belongs.
- Rely on undefined behavior in `symbol` to correctly print unprintable special characters by converting sexprs to strings and then converting those to symbols.
- Move `simple-type` and `drop-quote` to `splint.utils`.

## v1.9.0 - 2023-06-09

### New Rules

- `style/prefer-clj-string`: Prefer `clojure.string` functions over raw interop. Defaults to `true`.

### Added

- `--[no]summary` cli flag to print or not print the summary line.

### Changed

- `:filename` in `Diagnostic` is now a `java.io.File` object, not a string. This is propogated through everything. I suspect no one is using these so I think I could change the `Diagnostic` as well, but maybe I'll wait a min.
- `make-edamame-opts` now accepts both `features` and `ns-state`, and `parse-string` and `parse-string-all` take in `features` instead of `ns-state`.
- The runner tracks the filetype of each file and runs over `cljc` files twice, both `clj` and `cljs`, with their respective sides of the reader conditionals applied.
- Diagnostics are deduped before printing.
- `lint/warn-on-reflection` only runs in `clj` files.
- Remove `farolero`. Didn't provide any benefits over judicious `try`/`catch` use. :(
- Extend the `matcher-combinators.core/Matcher` protocol to `java.io.File`, making `match?` work nicely with both strings and file objects.
- Performance improvements by converting `rules-by-type` from a map of simple-type -> map of rule name -> rule to simple-type -> vec of rule.

### Fixed

- Correctly print special characters/clojure.core vars (`@`, not `splint/deref`, etc).

## v1.8.0 - 2023-05-30

### New rules

- `lint/warn-on-reflection`: Require that `(set! *warn-on-reflection* true)` is called after the `ns` declaration at the start of every file. Defaults to `false`.

### Breaking

- Deprecate `--config`. Add `--print-config`. No timeline for removal of `--config` (maybe never?).

### Added

- `edn` / `edn-pretty` output: Print diagnostics as edn using `clojure.core/prn` and `clojure.pprint/pprint`.
- Continue to process files after running into errors during rules checking.

### Changed

- Dependencies are updated to latest.
- `json` and `json-pretty` keys are now sorted.
- Small performance improvements to patterns.

## v1.7.0 - 2023-05-26

### New Rules

- `metrics/parameter-count`: Function parameter vectors shouldn't have more than 4 positional parameters. Has `:positional` and `:include-rest` styles (only positional or include `& args` rest params too?), and `:count` configurable value to set maximum number of parameters allowed.

### Added

- Add Metrics rules to documentation.
- Add `-s` / `--silent` command line flag to print literally nothing when running Splint.
- `json` output: Print diagnostics as json using `clojure.data.json`.
- `json-pretty` output: Same as `json` but prettified with `pprint`.
- Track processed files in `:checked-files`.
- Add initial `corpus` files to handle large-scale tests.

### Changed

- Move Splint-specific dev code to proper namespaces in `dev/`.
- Extract `splint.runner/run-impl` to decomplect processing cli options and returning a status code from performing the actual config loading and rule building and running.
- Rewrite test helper `check-all` to properly call the existing architecture instead of mock it, to accurately test the `run-impl` flow.
- Use [farolero](https://github.com/IGJoshua/farolero/) to handle `splint.runner` errors. Shows no signs of slowing down the app, so will investigate other areas for usage as well.

### Fixed

- Only attach parsed `defn` metadata when fn name exactly matches `defn` or `defn-` and second form is a symbol.
- `--no-parallel` was producing a lazy seq, now consumes to actually check all files. Oops lol.
- Map over top-level forms with `nil` parent form instead of treating the whole file as a top-level vector of forms. Fixes `naming/lisp-case`.
- Add pre- and post- `attr-maps` to `defn` metadata when parsing `defn` forms.
- Added license headers where necessary.

## v1.6.1 - 2023-05-22

### Fixed

- Re-fix deploy script.

## v1.6.0 - 2023-05-22

### Added

- Multiple self tests for consistency.
- New test runner based on Cognitect test-runner to print better summary and skip printing namespaces.

### Changed

- Cleaned up deploy recipe.
- Wrote short descriptions for all empty config.edn rule descriptions.
- Removed tools.cli defaults for `--parallel` and `--output`, now those are added later (see [#5](https://github.com/NoahTheDuke/splint/issues/5)).

### Fixed

- Correctly merge cli and local options ([#5](https://github.com/NoahTheDuke/splint/issues/5)).
- Edge cases for `lint/if-not-do`, `style/when-not-do`.

## v1.5.0 - 2023-05-12

### New Rules

- `metrics/fn-length`: Function bodies shouldn't be longer than 10 lines. Has `:body` and `:defn` styles, and `:length` configurable value to set maximum length.

### Added

- Add `test-helpers/expect-match` to assert on submatches, transition all existing `check-X` functions to use it instead.
- Track end position of diagnostics.
- Attach location metadata to function "arities" when a defn arg+body isn't wrapped in a list.

### Changed

- Parse `defn` forms in postprocessing and attach as metadata instead of parsing in individual rules.

### Fixed

- Fix `style/multiple-arity-order` with `:arglists` metadata.
- Fix binding pattern when binding is falsey.
- Skip `#(.someMethod %)` in `lint/fn-wrapper`.
- Skip `and` and `or` in `style/prefer-condp`.

## v1.4.1 - 2023-05-12

### Fixed

- Fix `io/resource` issue.
- Remove `.class` files from `jar`.

## v1.4.0 - 2023-05-08

### Added

- `-v` and `--version` cli flags to print the current version.
- `--config TYPE` cli flag to print the `diff`, `local`, or `full` configuration.

### Fixed

- Fix "Don't know how to create ISeq from: clojure.lang.Symbol" error in `splint.rules.helpers.parse-defn` when trying to parse ill-formed function definitions.
- "Fix" error messages. Honestly, I'm not great at these so I'm not entirely sure how to best display this stuff.
- Skip `#(do [%1 %2])` in `style/useless-do`, add docstring note about it.

## v1.3.2 - 2023-04-28

### Fixed

- Babashka compatibility
- Set up Github CI

## v1.3.1 - 2023-04-27

### Fixed

- Links in docs for style guide.

## v1.3.0 - 2023-04-27

### New Rules

- `naming/single-segment-namespace`: Prefer `(ns foo.bar)` to `(ns foo)`.
- `lint/prefer-require-over-use`: Prefer `(:require [clojure.string ...])` to `(:use clojure.string)`. Accepts different styles in the replacement form: `:as`, `:refer [...]` and `:refer :all`.
- `naming/conventional-aliases`: Prefer idiomatic aliases for core libraries (`[clojure.string :as str]` to `[clojure.string :as string]`).
- `naming/lisp-case`: Prefer kebab-case over other cases for top-level definitions. Relies on [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).
- `style/multiple-arity-order`: Function definitions should have multiple arities sorted fewest arguments to most: `(defn foo ([a] 1) ([a b] 2) ([a b & more] 3))`

## v1.2.4 - 2023-04-24

### Fixed

- Parsing bug in `lint/fn-wrapper` introduced in v1.2.3.

## v1.2.3 - 2023-04-24

### Added

- `*warn-on-reflection*` to all rules and rule template.
- Use `:spat/import-ns` metadata as way to track when a symbol has been imported.

### Changed

- Various performance enhancements:
  - Use protocols in `noahtheduke.spat.pattern/simple-type` for performance.
  - Use `volatile` instead of `atom` for bindings in `noahtheduke.spat.pattern`.
  - Switch `keep` to `reduce` to avoid seq and laziness manipulation.
  - Use `some->` where appropriate for short-circuiting.

### Fixed

- Fix [#2](https://github.com/NoahTheDuke/splint/issues/2), false positive on interop fn-wrappers.
- Lots of small namespace parsing fixes.

## v1.2.2 - 2023-04-13

### Fixed

- Differentiate between `&&.` rest args and parsed lists in `:on-match` handlers by attaching `:noahtheduke.spat.pattern/rest` metadata to bound rest args.
- Bump `edamame` to v1.3.21 to handle `#:: {:a 1}` auto-resolved namespaced maps with spaces between the colons and the map literal.
- Use correct url in install docs. (Thanks [@dpassen](https://github.com/dpassen))

## v1.2.1 - 2023-04-07

### Added

- `lint/thread-macro-one-arg` supports `:inline` and `:avoid-collections` styles.
- `:updated` field in configuration edn, show in rule docs.
- `:guide-ref` for `style/prefer-clj-math`.
- Interpose `<hr>` between each rule's docs.

### Changed

- Clarify docstring for `lint/dorun-map`.

### Fixed

- Left align contents of tables in rule docs.
- Correctly render bare links in rule docs.
- Correctly export clojars info in `deploy` justfile recipe.

## v1.2.0 - 2023-04-06

### Added

- `markdown` output: Same text as `full` but with a fancy horizontal bar, header, and code blocks.
- `:chosen-style` allows for rules to have configuration and different "styles". The first supported is `lint/not-empty?` showing either `seq` or `not-empty`.

### Changed

- `ctx` is no longer an atom, but a plain map. The `:diagnostics` entry is now the atom.
- `splint.runner/check-form` returns the entire updated `ctx` object instead of just the diagnostics. (I'm not entirely sure that's reasonable, but it's easily changed.)
- Move a lot of rules from `lint` to `style` genre:
  - `apply-str`
  - `apply-str-interpose`
  - `apply-str-reverse`
  - `assoc-assoc`
  - `conj-vector`
  - `eq-false`
  - `eq-nil`
  - `eq-true`
  - `eq-zero`
  - `filter-complement`
  - `filter-vec-filterv`
  - `first-first`
  - `first-next`
  - `let-do`
  - `mapcat-apply-apply`
  - `mapcat-concat-map`
  - `minus-one`
  - `minus-zero`
  - `multiply-by-one`
  - `multiply-by-zero`
  - `neg-checks`
  - `nested-addition`
  - `nested-multiply`
  - `next-first`
  - `next-next`
  - `not-eq`
  - `not-nil`
  - `not-some-pred`
  - `plus-one`
  - `plus-zero`
  - `pos-checks`
  - `tostring`
  - `update-in-assoc`
  - `useless-do`
  - `when-do`
  - `when-not-call`
  - `when-not-do`
  - `when-not-empty`
  - `when-not-not`

### Breaking

- Add `ctx` as first argument to `:on-match` functions to pass in config to rules. Update functions in `splint.runner` as necessary.

## v1.1.1 - 2023-03-31

### Changed

- Update Rule Documentation.
- Include new documentation in cljdoc.edn

## v1.1.0 - 2023-03-31

### Added

- Write documentation for rules and patterns.
- Write docstrings for a bunch of `noahtheduke.spat.pattern` functions.
- Include outside links in config in rules docs.
- Check `:spat/lit` metadata to treat special symbols in pattern DSL as their literal values.

### Changed

- Attempt to resolve predicates in calling namespace first, then in `clojure.core`, then in `noahtheduke.splint.rules.helpers`.
- Rename read-dispatch type from `:var` to `:binding`.

## v1.0.1 - 2023-03-22

### Fixed

- Run linting over syntax-quoted forms again.

## v1.0 - 2023-03-22

### New Rules

- `style/def-fn`: Prefer `(let [z f] (defn x [y] (z y)))` over `(def x (let [z f] (fn [y] (z y))))`
- `lint/try-splicing`: Prefer `(try (do ~@body) (finally ...))` over `(try ~@body (finally ...))`.
- `lint/body-unquote-splicing`: Prefer `(binding [max mymax] (let [res# (do ~@body)] res#))` over `(binding [max mymax] ~@body)`.

### Added

- Use markdownlint to pretty up the markdown in the repo. Will do my best to keep up with it.

### Changed

- Add `--parallel` and `--no-parallel` for running splint in parallel or not. Defaults to `true`.
- No longer run linting over quoted or syntax-quoted forms.
- Rely on [edamame][edamame]'s newly built-in `:uneval` config option for `:splint/disable`.
- Move version from `build.clj` to `resources/SPLINT_VERSION`.

### Fixed

- `naming/record-name`: Add `:message`.
- `style/prefer-condp`: Only runs if given more than 1 predicate branch.
- `style/set-literal-as-fn`: Allow quoted symbols in sets.

## v0.1.119 - 2023-03-16
Actually wrote out something of a changelog.

### New Rules

- `lint/duplicate-field-name`: `(defrecord Foo [a b a])`
- `naming/conversion-functions`: Should use `x->y` instead of `x-to-y`.
- `style/set-literal-as-fn`: Should use `(case elem (a b) true false)` instead of `(#{'a 'b} elem)`

### Added

- The `:new-rule` task now creates a test stub in the correct test directory.
- `#_:splint/disable` is treated as metadata on the following form and disables all rules. `#_{:splint/disable []}` can take genres of rules (`[lint]`) and/or specific rules (`[lint/loop-do]`) and disables those rules. See below (Thoughts and Followup) for discussion and [Configuration](./docs/configuration.md) for more details.

### Changed

- `defrule` now requires the provided rule-name to be fully qualified, and doesn't perform any `*ns*` magic to derive the genre.
- Add support for specifying `:init-type` in `defrule` to handle symbol matching.
- All of the `:dispatch` reader macros provided by Edamame now wrap their sexps in the appropriate `(splint/X sexp)` form, to distinguish them from the symbol forms. Aka `#(inc %)` is now rendered as `(splint/fn [%1] (inc %1))`, vs the original `(fn* ...)`, or `#'x` is now `(splint/var x)` vs `(var x)`. This allows for writing rules targeting the literal form instead of the symbol form, and requires that rule patterns rely on functions in `noahtheduke.splint.rules.helpers` to cover these alternates.
- Split all rules tests into their own matching namespaces.
- Add `noahtheduke.splint.rules.helpers` as an autoresolving namespace so rules can use predicates defined within it without importing or qualifying.
- Renamed errors from `violation` to `diagnostic`.
- Merge rules configs into rules maps at load-time.

### Fixed

- `lint/duplicate-field-name` wasn't checking that `?fields` was a vector before calling `count` on it.

### Thoughts
I want another parser because I want access to comments. Without comments, I can't parse magic comments, meaning I can't enable or disable rules inline, only globally. That's annoying and not ideal. However, every solution I've dreamed up has some deep issue.

- [Edamame][edamame] is our current parser and it's extremely fast (40ms to parse `clojure/core.clj`) but it drops comments. I've forked it to try to add them, but that would mean handling them in every other part of the parser, such as syntax-quote and maps and sets, making dealing with those objects really hard. :sob:

- [Rewrite-clj](https://github.com/clj-commons/rewrite-clj) only exposes comments in the zip api, meaning I have to operate on the zipper objects with zipper functions (horrible and slow). It's nice to rely on Clojure built-ins instead of `(loop [zloc zloc] (z/next* ...))` nonsense.

- [clj-kondo](https://github.com/borkdude/clj-kondo) is faster than rewrite-clj and has a nicer api, but the resulting tree isn't as easy to work with as Edamame and it's slower. Originally built Spat in it and found it to be annoying to use.

- [parcera](https://github.com/carocad/parcera) looked promising, but the pre-processing in `parcera/ast` is slow and operating on the Java directly is deeply cumbersome. The included grammar also makes some odd choices and I don't know ANTLR4 well enough to know how to fix them (such as including the `:` in keyword strings). Additionally, if I were to switch, I would have to update/touch every existing rule.

### Followup
After tinkering with Edamame for a bit, I've found a solution that requires no changes to [edamame][edamame] to support: `#_:splint/disable`. This style of directive applies metadata to the following form: `#_{:splint/disable [lint/plus-one]} (+ 1 x)`. Edamame normally discard `#_`/discarded forms, so on Borkdude's recommendation, I use `str/replace` to convert it at parse-time to metadata. This uses an existing convention and handles the issue of disabling multiple items or disabling for only a certain portion of the file.

## v0.1.85
Update readme with some better writing.

### New Rules

- `dev/sorted-rules-require` for internal use only

### Changed

- Annotate all rules with `:no-doc`.
- Rename `lint/cond-else` to `style/cond-else`.
- Cleaned up readme.

## v0.1.69
Renamed to Splint! Things are really coming together now.

### New Rules

- `lint/assoc-in-one-arg`
- `lint/update-in-one-arg`
- `naming/predicate`
- `naming/record-name`
- `style/let-if`
- `style/let-when`
- `style/new-object`
- `style/prefer-boolean`
- `style/prefer-condp`
- `style/redundant-let`
- `style/single-key-in`

### Added

- Basic CLI.
- Basic config file and config management.
- cljdoc support.
- `-M:gen-docs` for rule documentation generation and formatting.
- `-M:new-rule` task to generate a new rule file from a template.
- `-M:deploy` task to push to clojars.

### Changed

- Split main file into multiple files: core functionality to namespaces, each rule to a separate file.
- Rename `lint/with-meta-vary-meta` to `style/prefer-vary-meta`.
- Rename `lint/thread-macro-no-arg` to `lint/redundant-call`.

## v0.1 - 2023-02-16
Initial release of `spat`, announcement on Clojurian Slack and bbin installation set up. Contains working pattern matching system, a bunch of rules, and a simple runner.

[edamame]: https://github.com/borkdude/edamame
