# Change Log
This changelog is loose. Versions are not semantic, they are incremental. Splint is not meant to be infrastructure, so don't rely on it like infrastructure; it is a helpful development tool.

## [Unreleased]

## [v1.0]

### Added

- Use markdownlint to pretty up the markdown in the repo. Will do my best to keep up with it.

### New Rules

- `style/def-fn`: Prefer `(let [z f] (defn x [y] (z y)))` over `(def x (let [z f] (fn [y] (z y))))`
- `lint/try-splicing`: Prefer `(try (do ~@body) (finally ...))` over `(try ~@body (finally ...))`.
- `lint/body-unquote-splicing`: Prefer `(binding [max mymax] (let [res# (do ~@body)] res#))` over `(binding [max mymax] ~@body)`.

### Changed

- Add `--parallel` and `--no-parallel` for running splint in parallel or not. Defaults to `true`.
- No longer run linting over quoted or syntax-quoted forms.
- Rely on [edamame][edamame]'s newly built-in `:uneval` config option for `:splint/disable`.
- Move version from `build.clj` to `resources/SPLINT_VERSION`.

### Fixed

- `naming/record-name`: Add `:message`.
- `style/prefer-condp`: Only runs if given more than 1 predicate branch.
- `style/set-literal-as-fn`: Allow quoted symbols in sets.

## [v0.1.119]
Actually wrote out something of a changelog.

### Added

- The `:new-rule` task now creates a test stub in the correct test directory.
- `#_:splint/disable` is treated as metadata on the following form and disables all rules. `#_{:splint/disable []}` can take genres of rules (`[lint]`) and/or specific rules (`[lint/loop-do]`) and disables those rules. See below (Thoughts and Followup) for discussion and [Configuration](./docs/configuration.md) for more details.

### New Rules

- `lint/duplicate-field-name`: `(defrecord Foo [a b a])`
- `naming/conversion-functions`: Should use `x->y` instead of `x-to-y`.
- `style/set-literal-as-fn`: Should use `(case elem (a b) true false)` instead of `(#{'a 'b} elem)`

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

## [v0.1.85]
Update readme with some better writing.

### Added

### New Rules

- `dev/sorted-rules-require` for internal use only

### Changed

- Annotate all rules with `:no-doc`.
- Rename `lint/cond-else` to `style/cond-else`.
- Cleaned up readme.

## [v0.1.69]
Renamed to Splint! Things are really coming together now.

### Added

- Basic CLI.
- Basic config file and config management.
- cljdoc support.
- `-M:gen-docs` for rule documentation generation and formatting.
- `-M:new-rule` task to generate a new rule file from a template.
- `-M:deploy` task to push to clojars.

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

### Changed

- Split main file into multiple files: core functionality to namespaces, each rule to a separate file.
- Rename `lint/with-meta-vary-meta` to `style/prefer-vary-meta`.
- Rename `lint/thread-macro-no-arg` to `lint/redundant-call`.

## [v0.1] - 2023-02-16
Initial release of `spat`, announcement on Clojurian Slack and bbin installation set up. Contains working pattern matching system, a bunch of rules, and a simple runner.

[edamame]: https://github.com/borkdude/edamame

[Unreleased]: https://github.com/noahtheduke/splint/compare/v1.0...HEAD
[v1.0]: https://github.com/noahtheduke/splint/compare/v0.1.119...v1.0
[v0.1.119]: https://github.com/noahtheduke/splint/compare/0.1.85...v0.1.119
[v0.1.85]: https://github.com/noahtheduke/splint/compare/v0.1.69...v0.1.85
[v0.1.69]: https://github.com/noahtheduke/splint/compare/v0.1...v0.1.69
[v0.1]: https://github.com/NoahTheDuke/splint/tree/v0.1
