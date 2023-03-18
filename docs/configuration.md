# Configuration

## Command-line options

* `-h`, `--help`: Print the command line options.
* `-o`, `--output FMT`: Output format: `simple`, `full`, `clj-kondo`. Defaults to `full`.
* `-q`, `--quiet`: Print no suggestions, only return exit code.
* `--[no-]parallel`: Run Splint in parallel. Defaults to `true`.

### Output styles

**simple:**
A description of the file path, line and column within the file, the name of the rule, and the message of the rule.
```
test/clj/game/core/say_test.clj:18:15 [lint/eq-zero] - Use `zero?` instead of recreating it.
```

**full:**
A description of the file path, line and column within the file, the name of the rule, and the message of the rule. And then the existing form and the suggested/replaced form demonstrating the necessary change.
```
test/clj/game/core/say_test.clj:18:15 [lint/eq-zero] - Use `zero?` instead of recreating it.
(= 0 (get-counters (refresh pb) :advancement))
Consider using:
(zero? (get-counters (refresh pb) :advancement))
```

**clj-kondo:**
A description of the file path, line and column within the file, the word `warning`, and the message of the rule.
```
test/clj/game/core/say_test.clj:18:15: warning: Use `zero?` instead of recreating it.
```

## Configuration file

All of the above command-line options and the rules options can be defined in a `.splint.edn` file at the root of the project.

The format of the file is an `edn` map. Keys must be symbols and any rules configuration must be fully-qualified:

```clojure
{parallel true
 lint/eq-nil {:enabled false}}
```

Some rules have specific options or styles that can be set. These are detailed in the full rule pages.

## Inline

A single rule can be disabled in the following form with `#_:splint/disable`:

```clojure
#_:splint/disable (+ 1 x)
```

Entire genres of rules and specific rules can be disabled in the same manner by using the map form:

```
#_{:splint/disable [style lint/plus-one]} (do (+ 1 x))
````
This will disable all `style` rules and the specific `lint/plus-one` rule in the following form. The inline rule doesn't apply to any further forms, so there's no need for a `#_:splint/enable`, like in other linters.

## Merging options

When identifying whether to apply a given rule to a form, the provided sources of configuration are merged: Command line options override `.splint.edn`, and inline options override command line options.
