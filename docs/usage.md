# Basic Usage

The primary usage and entrypoint is through the `-M` cli interface, tho `-X` support and a programmatic entrypoint is offered through `noahtheduke.splint/x-main`.

## -M

```text
$ clojure -M:splint --help
splint v1.25.0

Usage:
  clojure -M:splint [options]
  clojure -M:splint [options] [path...]
  clojure -M:splint [options] -- [path...]

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

### Command-line options

* `-o`, `--output FMT`: Output format: `simple`, `full`, `clj-kondo`, `markdown`, `json`, `json-pretty`. Defaults to `full`. (See below for description of each.)
* `-r`, `--require FILE`: Require additional custom rules by loading specified files. Can be provided multiple times. (See [Writing a new rule][new rule] for further details.)
* `--only RULE`: Run only the specified rule(s) or genre(s).
* `--[no-]parallel`: Run Splint in parallel. Defaults to `true`.
*  `--lang LANG`: Which dialect to check? If `cljc`, will check both `clj` and `cljs` files/branches. Defaults to `cljc`.
* `--autocorrect`: Automatically apply safe changes. (See the [Rules Overview][rules] for more details.)
* `--interactive`: Run autocorrect interactively. (Does nothing if `--autocorrect` is not passed.)
* `--[no-]summary`: Print summary. Defaults to `true`.
* `-q`, `--quiet`: Print no diagnostics.
* `-s`, `--silent`: Print no diagnostics or summary. Overrides `--quiet` and `--summary`.
* `--errors`: Limits printed diagnostics to parsing or internal Splint errors.
* `--print-config TYPE`: Print the absolute path of the loaded config, and prints the contents of the loaded config according to the chosen type: `diff` for the difference between default and loaded config file, `local` for the contents of the loaded config file, and `full` for the merged default and loaded config file.
* `--auto-gen-config`: Generate a passing config file for chosen paths. (See [configuration][configuration] for more details.)
* `-h`, `--help`: Print the command line options.
* `-v`, `--version`: Print the version (useful for other tools).

Some of the above options can be set in the [configuration file][configuration]. See that page for further details.

[configuration]: configuration.md
[new rule]: develop-new-rule.md#writing-a-new-rule
[rules]: rules-overview.md

## -X

```text
$ clojure -X:splint :help true
splint v1.25.0

Usage:
  clojure -X:splint [key-path/value pairs]

Options:
...
```

As above, `splint` will read `deps.edn` or `project.clj` if no `:paths` are passed, and diagnostics will be printed according to `:output`. If an diagnostics are raised, an exception is thrown instead of exiting with a non-zero number:

```
$ clojure -X:splint :paths '["src"]'
src/noahtheduke/splint/parser/ns.clj:171:7 [style/plus-one] - Use `inc` instead of recreating it.
(+ 1 1)
Consider using:
(inc 1)

Linting took 502ms, checked 142 files, 1 style warnings
Execution error (ExceptionInfo) at noahtheduke.splint/x-main (splint.clj:156).
Failed run

Full report at:
/var/folders/mp/l5hpkwss6p749m_74mk2ksl80000gn/T/clojure-17811884274698531129.edn
```

### Shape of expected inputs

The above `-M` cli args are available nearly one-to-one in the `-X` form. See above for descriptions of them. The main difference is that instead of passing paths as trailing args, they must be passed in an explicit sequence of strings. (Note quoting rules as discussed in the [Clojure CLI Reference][cli-ref].)

* `:paths ["paths"]`
* `:output "fmt"`
* `:require ["paths"]`
* `:only [rules]`
* `:parallel`
* `:lang "lang"`
* `:autocorrect`
* `:interactive`
* `:summary`
* `:quiet`
* `:silent`
* `:errors`
* `:print-config "type"`
* `:auto-gen-config`
* `:help`
* `:version`

[cli-ref]: https://clojure.org/reference/clojure_cli#quoting

## Output styles

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

## Config styles

**diff:**
Only prints the difference between the Splint defaults and the loaded `.splint.edn` file. This is useful when explicitly setting local options to their default.

**local:**
Prints the contents of the loaded `.splint.edn` file. Useful to see which options are being set without directly opening the file.

**full:**
Fully merges the Splint defaults with the loaded `.splint.edn` file and prints it.
