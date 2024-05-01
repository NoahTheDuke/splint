# Rules

The various checks that Splint performs are called "rules". Each rule looks for a single suspicious form or multiple variations of one, called a `:pattern` or `:patterns` for multiple. Splint checks the pattern against every appropriate form in every file it checks, and if there's a match, it calls the function `:on-match` to potentially create and store a [Diagnostic].

[Diagnostic]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.diagnostic

When they're initialized, rules store the type of data that they're initially looking for, called the `:init-type`. This helps limit the number of rules Splint has to check for each form. (For example, when looking at `(foo bar)`, only rules with `:init-type :list` will be selected.) `:init-type` is automatically derived from the provided `:patterns`, but can be deliberately specified to help in ambiguous situations.

For each file, Splint performs a depth-first descent into every form, starting with any full-file rules that operate on a vector of the top-level forms (`:init-type :file`). Quoted forms are not checked, but syntax-quoted forms are. This is because quoted forms aren't usually intended to be evaluated code, but syntax-quoted forms are.

## Writing a new rule

Rules are fairly simple. The call to `defrule` takes a fully qualified name (namespace being the rule's genre), a docstring with examples, and the rule map. The resulting "rule" is both stored in the [`global-rules`] atom as well as defined with `def` as a var in the calling namespace (dropping the rule-name's namespace).

[`global-rules`]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.rules#global-rules

To load them, use the `-r`/`--require` command line arg with `-r path/to/file.clj` or in the config file with `{require ["path/to/file.clj"]}`. The specified files are loaded with `clojure.core/load-file` at run-time. (There is currently no support for loading rules from jars or dependencies.) The rule must be enabled in `.splint.edn` for it to be used, either by rule name (`example/new-rule {:enabled true}`) or by genre (`example {:enabled true}`).

**WARNING**: Unlike other tools, this doesn't use [SCI][SCI]. Loading arbitrary code is inherently unsafe, so don't load code you don't know!!!!!!

[SCI]: https://github.com/babashka/sci

### Rule name and genre

Rule names should be short descriptions of the problem domain or the preferred solution. Genres are meant to loosely group rules according to their focus.

The genres are described in [Genre Overview](genre-overview.md).

### Docstrings

Docstrings are rendered with cljdoc's markdown parser, and there's light preprocessing that extracts the code examples and wraps them in code blocks to give them syntax highlighting. The text of the docstring should give only as much detail as necessary for someone new to the rule's intent. If there's a larger discussion online, link to it in the config edn file.

For example, "[How to ns]" is a popular article about how to structure `ns` forms. The docstring of a potential `style/how-to-ns` should not include it in full, the docstring should say something like, "[Alessandra Sierra]'s recommended `ns` style recommends putting :refer-clojure first, :require second, and :import third. Use vectors with :requires, and lists with :import. Please see the link for further details."

[How to ns]: https://stuartsierra.com/2016/clojure-how-to-ns.html
[Alessandra Sierra]: https://www.lambdasierra.com/2022/hello

Examples have a specific format they must follow. `Examples:` must be surrounded by blank lines, and then alternating `# bad` and `# good` lines, with clojure code directly underneath each.

For example, `style/style/set-literal-as-fn`:

```clojure
  "...

  Examples:

  ; bad
  (#{'a 'b 'c} elem)

  ; good
  (case elem (a b c) elem nil)
  "
```

### Rule map

The rule name has two major requirements: either a `:pattern` form or a `:patterns` vector of forms, and either a `:replace` pattern or an `:on-match` function.

Patterns are described in detail in [Patterns](patterns.md), but to give a brief overview, they are quoted forms that are compiled into functions that return `nil` if no match is found or a map (empty if no bindings are used or full of symbol-to-bound value pairs if bindings are used). Literals are matched with `=`, collection literals (lists, vectors, maps, and sets) have their type and size checked and then each expected internal is matched the same way. Any symbol that starts with a question-mark is treated as a binding.

For example, if you want to match on an `if` that has `nil` in the else branch, you would write `'(if ?pred ?truthy nil)`. This is converted into a function that checks (in this order): The form is a list. The list is 4 elements long. The first element is the symbol `'if`. The second element can be any value and is bound to the symbol `?pred`. The third element can be any value and is bound to the symbol `?truthy?`. The fourth element is `nil`.

The macro [`noahtheduke.splint.pattern/pattern`] builds the matching function.

[`noahtheduke.splint.pattern/pattern`]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.pattern#pattern

A simple replacement pattern is defined with `:replace`. This can be a quoted form of any shape and will be substituted in when printing diagnostics. It uses some of the same logic as the patterns. For our purposes, the main one is that any symbols that start with a quotation mark is substituted for the value of that symbol in the binding output from the `:pattern`. (The rest of the details are also in [Patterns](patterns.md).)

For example, if the pattern `'(if ?pred ?truthy nil)` matches, then the replacement could be `'(when ?pred ?truthy)`, which would insert the values that `?pred` and `truthy` matched on into the replacement form.

To handle more complex replacement logic, use `:on-match`. It must be a function that accepts 4 arguments: the context object (`ctx`), the currently matched rule object, the current form that is being evaluated, and the bindings map from the pattern. Unlike the limited DSL available in the patterns, this is regular Clojure and can perform any action one likes.

If `:on-match` returns a [Diagnostic], then the diagnostic is stored in the context. The helper function [`->diagnostic`] takes the rule and form to point to the right spot in the code, and can optionally take a map with `:replace-form` and `:message` keys to set the diagnostic's reported alternative form and to override the rule's message object with a more specific message to print in all diagnostics.

[`->diagnostic`]: https://cljdoc.org/d/io.github.noahtheduke/splint/CURRENT/api/noahtheduke.splint.diagnostic#->diagnostic

If `:on-match` does not return a [Diagnostic], then the rule is considered to have not made a match (even tho the pattern returned a logical value). This allows for further refinement when writing rules without having to extend the pattern language unduly.
