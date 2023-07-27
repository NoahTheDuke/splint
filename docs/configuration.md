# Configuration

`splint` has default behavior which can be altered by modifying a `.splint.edn` file at the root of the project. Rules can be enabled or disabled or have specific options set.

The format of the file is an `edn` map. Keys must be symbols and any rules configuration must be fully-qualified:

```clojure
{parallel true
 lint/eq-nil {:enabled false}
 lint/not-empty? {:chosen-style :not-empty}
```

Some rules have specific options or styles that can be set. These are detailed in the full rule pages.

The following command-line options can be set: `output`, `parallel`, `quiet`, `silent`, and `summary`. `output` requires a string and then rest booleans.

## Excluding files

`splint` checks every file recursively from the derived or provided file paths. This isn't always desirable, so specific paths or path globs can be excluded using `global {:excludes []}`. `:excludes` takes a vector of strings that it uses to exclude matching files. It relies on Java's [java.nio.file.PathMatcher](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-) glob or regex syntax, and will default to `"glob:"` if the syntax portion is not specified.

```clojure
{global {:excludes ["foo" "glob:**/bar.clj" "regex:[a-z].clj"]}}
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
