# Configuration

`splint` has default behavior which can be altered by modifying a `.splint.edn` file at the root of the project. Rules can be enabled or disabled or have specific options set.

The format of the file is an `edn` map. Keys must be symbols and any rules configuration must be fully-qualified:

```clojure
{parallel true
 output "full"
 lint/eq-nil {:enabled false}
 lint/not-empty? {:chosen-style :not-empty}
```

Some rules have specific options or styles that can be set. These are detailed in the full rule pages.

The following command-line options can be set: `output`, `parallel`, `quiet`, `require`, `silent`, and `summary`. `output` requires a string, `require` requires a vector of strings, and the rest booleans (as seen above).

## Excluding files

`splint` checks every file recursively from the derived or provided file paths. This isn't always desirable, so specific paths or path globs can be excluded using `{:excludes []}`. `:excludes` takes a vector of strings that it uses to exclude matching files. The strings can optionally specify a syntax to use in the form `prefix:pattern`. The supported syntaxes are:

| Prefix | Syntax | Match full path? | Notes |
| --- | --- | --- | --- |
| `glob` | [FileSystem.getPathMatcher](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)) | Yes | Uses [PathMatcher](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/PathMatcher.html)'s `matches`. |
| `regex` | [java.util.regex.Pattern](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html) | Yes | Uses [PathMatcher](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/PathMatcher.html)'s `matches`. |
| `re-find` | [java.util.regex.Pattern](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html) | No | So-named to match the behavior of [clojure.core/re-find](https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/re-find). |
| `string` | N/A | No | A fixed string checked against the full file path with [clojure.string/includes?](https://clojure.github.io/clojure/clojure.string-api.html#clojure.string/includes?).

If the prefix is not provided, the string is treated as the `re-find` syntax.

### Example nmatching logic

* `glob:foo.clj` matches `foo.clj` but does not match `aa/foo.clj`. `glob:**/foo.clj` matches `foo.clj` and `aa/foo.clj`.
* `regex:foo.clj` matches `foo.clj` but does not match `aa/foo.clj`. `regex:.*/foo.clj` matches `foo.clj` and `aa/foo.clj`.
* `re-find:foo.clj` matches `foo.clj` and `aa/foo.clj` and `aa/foo-clj`.
* `string:foo.clj` matches `foo.clj` and `aa/foo.clj` but does not match `foo-clj` or `aa/foo-clj`.

### Example config usage

```clojure
{global {:excludes ["foo" "glob:**/bar.clj" "regex:[a-z].clj"]}
 lint/eq-nil {:excludes ["glob:**/src/exclude_me.clj"]}}
```

`:excludes` can also be put into any rule config as well, which will exclude matching files only for that specific rule.

## Inline

A single rule can be disabled in the following form with `#_:splint/disable`:

```clojure
#_:splint/disable (+ 1 x)
```

Entire genres of rules and specific rules can be disabled in the same manner by using the map form:

```clojure
#_{:splint/disable [style lint/plus-one]} (do (+ 1 x))
````

This will disable all `style` rules and the specific `lint/plus-one` rule in the following form. The inline rule doesn't apply to any further forms, so there's no need for a `#_:splint/enable` like in other linters.

## Merging options

When identifying whether to apply a given rule to a form, the provided sources of configuration are merged: Command line options override `.splint.edn`, and inline options override command line options.

## Auto-Generated Configuration

If you wish to use Splint but have a large number of offenses, it can be helpful to "start from zero" and disable all of the rules that raise diagnostics. Instead of hand-crafting such a config file, use `--auto-gen-config`, which will run Splint over the chosen directories/files and then create a `.splint.edn` file that disables each failing rule. Each rule has a comment with the number of diagnostics and the `:description` and the available styles of the rule, which should provide enough information to make reasonable decisions about how to fix each one.
