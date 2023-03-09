# Change Log

## [Unreleased]

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
