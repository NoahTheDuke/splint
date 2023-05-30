# Configuration

## Command-line options

* `-o`, `--output FMT`: Output format: `simple`, `full`, `clj-kondo`, `markdown`, `json`, `json-pretty`. Defaults to `full`.
* `--[no-]parallel`: Run Splint in parallel. Defaults to `true`.
* `--print-config TYPE`: Print the absolute path of the loaded config, and prints the contents of the loaded config according to the chosen type: `diff` for the difference between default and loaded config file, `local` for the contents of the loaded config file, and `full` for the merged default and loaded config file.
* `-q`, `--quiet`: Print no suggestions, only summary.
* `-s`, `--silent`: Print no suggestions or summary.
* `-h`, `--help`: Print the command line options.
* `-v`, `--version`: Print the current version.

### Output styles

**simple:**
Prints the filepath and location within the file, the name of the rule, and the message of the rule.

```text
test/clj/game/core/say_test.clj:18:15 [lint/eq-zero] - Use `zero?` instead of recreating it.
```

**full:**
Prints the filepath and location within the file, the name of the rule, and the message of the rule. Then prints the existing form and the suggested/replaced form demonstrating the necessary change.

```text
test/clj/game/core/say_test.clj:18:15 [lint/eq-zero] - Use `zero?` instead of recreating it.
(= 0 (get-counters (refresh pb) :advancement))
Consider using:
(zero? (get-counters (refresh pb) :advancement))
```

**clj-kondo:**
Prints the filepath and location within the file, the word `warning`, and the message of the rule.

```text
test/clj/game/core/say_test.clj:18:15: warning: Use `zero?` instead of recreating it.
```

**markdown:**
Same as `full` but formatted to produce markdown, with the location and rule name in a header and the code wrapped in code blocks:

    ----

    #### test/clj/game/core/say_test.clj:82:15 [lint/eq-zero]

    Use `zero?` instead of recreating it.

    ```clojure
    (= 0 (get-counters (refresh pb) :advancement))
    ```

    Consider using:

    ```clojure
    (zero? (get-counters (refresh pb) :advancement))
    ```

which renders to:

----

#### test/clj/game/core/say_test.clj:82:15 [lint/eq-zero]

Use `zero?` instead of recreating it.

```clojure
(= 0 (get-counters (refresh pb) :advancement))
```

Consider using:

```clojure
(zero? (get-counters (refresh pb) :advancement))
```

**json:**
Prints diagnostics formatted to json with `clojure.data.json`. `:rule-name` is a string of the fully-qualified symbol. `:form` isn't raw from the file, it's the processed form, converted to string with `pr-str`. `:alt` is converted to string with `pr-str`.

```json
{"rule-name":"style/eq-zero","form":"(= 0 (get-counters (refresh pb) :advancement))","message":"Use `zero?` instead of recreating it.","alt":"(zero? (get-counters (refresh pb) :advancement))","line":18,"column":15,"end-row":18,"end-col":61,"filename":"../netrunner/test/clj/game/core/say_test.clj"}
```

**json-pretty:**
Same as `json` but uses `clojure.data.json`'s `pprint`.

```json
{"rule-name":"style/eq-zero",
 "form":"(= 0 (get-counters (refresh pb) :advancement))",
 "message":"Use `zero?` instead of recreating it.",
 "alt":"(zero? (get-counters (refresh pb) :advancement))",
 "line":18,
 "column":15,
 "end-row":18,
 "end-col":61,
 "filename":"../netrunner/test/clj/game/core/say_test.clj"}
```

### Config styles

**diff:**
Only prints the difference between the Splint defaults and the loaded `.splint.edn` file. This is useful when explicitly setting local options to their default.

**local:**
Prints the contents of the loaded `.splint.edn` file. Useful to see which options are being set without directly opening the file.

**full:**
Fully merges the Splint defaults with the loaded `.splint.edn` file and prints it.

## Configuration file

All of the above command-line options and the rules options can be defined in a `.splint.edn` file at the root of the project.

The format of the file is an `edn` map. Keys must be symbols and any rules configuration must be fully-qualified:

```clojure
{parallel true
 lint/eq-nil {:enabled false}
 lint/not-empty? {:chosen-style :not-empty}
```

Some rules have specific options or styles that can be set. These are detailed in the full rule pages.

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
