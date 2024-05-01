# Basic Usage

```text
$ clojure -M:splint --help
splint v1.15.0

Usage:
  splint [options]
  splint [options] [path...]
  splint [options] -- [path...]

Options:
...
```

`splint` can be run with or without passing in paths to check. If they are included, they must follow all other options.

Running `splint` without any paths will read the local `deps.edn` or `project.clj` file and check the directories and files listed in the primary `:paths` and the directories and paths in the `:dev` and `:test` aliases as well.

```text
$ clojure -M:splint
...
Linting took 485ms, checked 229 files, 10 style warnings
```

Pass in any number of files or directories to lint them instead, ignoring the available project files.

```text
$ clojure -M:splint src/noahtheduke/splint.clj test/
...
Linting took 241ms, checked 115 files, 5 style warnings
```

## Command-line options

* `-o`, `--output FMT`: Output format: `simple`, `full`, `clj-kondo`, `markdown`, `json`, `json-pretty`. Defaults to `full`.
* `-r`, `--require FILE`: Require additional custom rules by loading specified files. Can be provided multiple times. (See [Writing a new rule](rules.md#writing-a-new-rule) for further details.)
* `--[no-]parallel`: Run Splint in parallel. Defaults to `true`.
* `-q`, `--quiet`: Print no diagnostics, only summary.
* `-s`, `--silent`: Print no diagnostics or summary.
* `--[no-]summary`: Don't print summary. Defaults to `true`.
* `--errors`: Limits printed diagnostics to parsing or internal Splint errors.
* `--print-config TYPE`: Print the absolute path of the loaded config, and prints the contents of the loaded config according to the chosen type: `diff` for the difference between default and loaded config file, `local` for the contents of the loaded config file, and `full` for the merged default and loaded config file.
* `-h`, `--help`: Print the command line options.
* `-v`, `--version`: Print the current version.

Some of the above options can be set in the [configuration file](configuration.md). See that page for further details.

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
