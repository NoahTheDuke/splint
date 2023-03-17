# Change Log
This changelog is loose. Versions are not semantic, merely perfunctory. Splint is not meant to be infrastructure, so don't rely on it like infrastructure; it's merely a helpful development tool.

## [Unreleased]
Actually wrote out something of a changelog.

## Added
- The `:new-rule` task now creates a test stub in the correct test directory.

### New Rules
- `naming/conversion-functions`: Should use `x->y` instead of `x-to-y`.
- `lint/duplicate-field-name`: `(defrecord Foo [a b a])`

## Changed
- `defrule` now requires the provided rule-name to be fully qualified, and doesn't perform any `*ns` magic to derive the genre.
- Add support for specifying `:init-type` in `defrule` to handle symbol matching.
- All of the `:dispatch` reader macros provided by Edamame now wrap their sexps in the appropriate `(splint/X sexp)` form, to distinguish them from the symbol forms. Aka `#(inc %)` is now rendered as `(splint/fn [%1] (inc %1))`, vs the original `(fn* ...)`, or `#'x` is now `(splint/var x)` vs `(var x)`. This allows for writing rules targeting the literal form instead of the symbol form.
- Split all rules tests into their own matching namespaces.
- Add `noahtheduke.splint.rules.helpers` as an autoresolving namespace so rules can use predicates defined within it without importing or qualifying.
- Renamed errors from `violation` to `diagnostic`.

## Thoughts
I want another parser because I want access to comments. Without comments, I can't parse magic comments, meaning I can't enable or disable rules inline, only globally. That's annoying and not ideal. However, every solution I've dreamed up has some deep issue.

* [Edamame](https://github.com/borkdude/edamame) is our current parser and it's extremely fast (40ms to parse `clojure/core.clj`) but it drops comments. I've forked it to try to add them, but that would mean handling them in every other part of the parser, such as syntax-quote and maps and sets, making dealing with those objects really hard. :sob:

* [Rewrite-clj](https://github.com/clj-commons/rewrite-clj) only exposes comments in the zip api, meaning I have to operate on the zipper objects with zipper functions (horrible and slow). It's nice to rely on Clojure built-ins instead of `(loop [zloc zloc] (z/next* ...))` nonsense.

* [clj-kondo](https://github.com/borkdude/clj-kondo) is faster than rewrite-clj and has a nicer api, but the resulting tree isn't as easy to work with as Edamame and it's slower. Originally built Spat in it and found it to be annoying to use.

* [parcera](https://github.com/carocad/parcera) looked promising, but the pre-processing in `parcera/ast` is slow and operating on the Java directly is deeply cumbersome. The included grammar also makes some odd choices and I don't know ANTLR4 well enough to know how to fix them (such as including the `:` in keyword strings). Additionally, if I were to switch, I would have to update/touch every existing rule.

### Followup
After tinkering with Edamame for a bit, I've found a solution that requires no changes to edamame to support: `#_:splint/disable`. This style of directive applies metadata to the following form: `#_{:splint/disable [lint/plus-one]} (+ 1 x)`. Edamame normally discard `#_`/discarded forms, so on Borkdude's recommendation, I use `str/replace` to convert it at parse-time to metadata. This uses an existing convention and handles the issue of disabling multiple items or disabling for only a certain portion of the file.

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

[Unreleased]: https://github.com/noahtheduke/splint/compare/0.1.85...HEAD
[v0.1.85]: https://github.com/noahtheduke/splint/compare/0.1.69...v0.1.85
[v0.1.69]: https://github.com/noahtheduke/splint/compare/v0.1...v0.1.69
[v0.1]: https://github.com/NoahTheDuke/splint/tree/v0.1
