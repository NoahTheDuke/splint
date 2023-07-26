# Change Log
This changelog is loose. Versions are not semantic, they are incremental. Splint is not meant to be infrastructure, so don't rely on it like infrastructure; it is a helpful development tool.

## Unreleased

## v1.10.1

### Fixed

- `performance/assoc-many` should only trigger when there are more than 1 pair.

## v1.10.0

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

## v1.9.0

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

## v1.8.0

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

## v1.7.0

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

## v1.6.1

### Fixed

- Re-fix deploy script.

## v1.6.0

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

## v1.5.0

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

## v1.4.1

### Fixed

- Fix `io/resource` issue.
- Remove `.class` files from `jar`.

## v1.4.0

### Added

- `-v` and `--version` cli flags to print the current version.
- `--config TYPE` cli flag to print the `diff`, `local`, or `full` configuration.

### Fixed

- Fix "Don't know how to create ISeq from: clojure.lang.Symbol" error in `splint.rules.helpers.parse-defn` when trying to parse ill-formed function definitions.
- "Fix" error messages. Honestly, I'm not great at these so I'm not entirely sure how to best display this stuff.
- Skip `#(do [%1 %2])` in `style/useless-do`, add docstring note about it.

## v1.3.2

### Fixed

- Babashka compatibility
- Set up Github CI

## v1.3.1

### Fixed

- Links in docs for style guide.

## v1.3.0

### New Rules

- `naming/single-segment-namespace`: Prefer `(ns foo.bar)` to `(ns foo)`.
- `lint/prefer-require-over-use`: Prefer `(:require [clojure.string ...])` to `(:use clojure.string)`. Accepts different styles in the replacement form: `:as`, `:refer [...]` and `:refer :all`.
- `naming/conventional-aliases`: Prefer idiomatic aliases for core libraries (`[clojure.string :as str]` to `[clojure.string :as string]`).
- `naming/lisp-case`: Prefer kebab-case over other cases for top-level definitions. Relies on [camel-snake-kebab](https://github.com/clj-commons/camel-snake-kebab).
- `style/multiple-arity-order`: Function definitions should have multiple arities sorted fewest arguments to most: `(defn foo ([a] 1) ([a b] 2) ([a b & more] 3))`

## v1.2.4

### Fixed

- Parsing bug in `lint/fn-wrapper` introduced in v1.2.3.

## v1.2.3

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

## v1.2.2

### Fixed

- Differentiate between `&&.` rest args and parsed lists in `:on-match` handlers by attaching `:noahtheduke.spat.pattern/rest` metadata to bound rest args.
- Bump `edamame` to v1.3.21 to handle `#:: {:a 1}` auto-resolved namespaced maps with spaces between the colons and the map literal.
- Use correct url in install docs. (Thanks [@dpassen](https://github.com/dpassen))

## v1.2.1

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

## v1.2.0

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

## v1.1.1

### Changed

- Update Rule Documentation.
- Include new documentation in cljdoc.edn

## v1.1.0

### Added

- Write documentation for rules and patterns.
- Write docstrings for a bunch of `noahtheduke.spat.pattern` functions.
- Include outside links in config in rules docs.
- Check `:spat/lit` metadata to treat special symbols in pattern DSL as their literal values.

### Changed

- Attempt to resolve predicates in calling namespace first, then in `clojure.core`, then in `noahtheduke.splint.rules.helpers`.
- Rename read-dispatch type from `:var` to `:binding`.

## v1.0.1

### Fixed

- Run linting over syntax-quoted forms again.

## v1.0

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

## v0.1.119
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
