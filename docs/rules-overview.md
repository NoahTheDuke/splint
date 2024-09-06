# Rules Overview

Splint rules are responsible for checking code for their particular issues. Each rule is in a "genre", that is to say, the loose grouping based on its focus. Their documentation has been split by genre to make searching and reading easier.

If a rule is enabled by default, then it will check your code (when it has not been disabled by local configuration) during a normal run. If a rule is safe, then the rule should be free of false positives. Rules will be marked as unsafe when static analysis can't determine if a usage is correct or not, or the suggested alternative code might be incorrect. If a rule has "Autocorrect", then it can be used with `--autocorrect`, and any diagnostics arising from that rule will be applied to the originating form in the source file.

**Note:** All rules capable of autocorrect are marked as safe. If a rule isn't safe for any reason, then it will not be enabled during an `--autocorrect` run. Some safe rules aren't capable of autocorrect, of course, but that's merely due to complexity or lack of implementation.

Any configuration or options for a given rule will be listed at the end. The "Name" is the key the chosen option should be put under in the local config. For example, given:

> **Configurable Attributes**
>
> | Name            | Default | Options                 |
> | --------------- | ------- | ----------------------- |
> | `:chosen-style` | `:dot`  | `:dot`, `:method-value` |

Add the below to `.splint.edn`:

```clojure
lint/dot-obj-method {:chosen-style :method-value}
```

## Genres

* [Lint](rules/lint.md) is for code that can be easily incorrect or is suspicious (`if` missing a branch, a threading macro with a single element, etc).
* [Metrics](rules/metrics.md) is about properties of code that can be measured, such as length of functions.
* [Naming](rules/naming.md) is about the names given to defining forms (`defn`, `defrecord`, etc) when they don't adhere to Clojure community idioms or style.
* [Performance](rules/performance.md) is for code that has faster alternatives at the cost of larger or more unwieldy code (`(assoc m :k1 v1 :k2 v2)` vs `(-> m (assoc :k1 v1) (assoc :k2 v2))`, etc). These are disabled by default as they are more contentious than the rest.
* [Style](rules/style.md) is for code that doesn't adhere to Clojure community idioms or style, especially when there are existing alternatives (`(when (not ...))` vs `(when-not ...)`, etc).
