# Splint

[![cljdoc badge](https://cljdoc.org/badge/noahtheduke/splint)](https://cljdoc.org/d/noahtheduke/splint)

**Split** is a Clojure static code analyzer and linter. It aims to warn about many of
the guidelines in the [Clojure Style Guide][style guide].

[style guide]: https://guide.clojure.style

## Why?

Why another Clojure linter? We have [clj-kondo][clj-kondo], [eastwood][eastwood], and
[kibit][kibit], in addition to [clojure-lsp][clojure-lsp]'s capabilities built on top of
clj-kondo. I have contributed to most of these, and recently took over maintenance of
kibit. However, most of them aren't built to be easily modifiable, and while kibit's
rules are simple, the underlying engine (built on [core.logic][core.logic]) is quite
slow. This means that adding or updating the various linting rules can be quite
frustrating and taxing.

Inspired by [RuboCop][rubocop], I decided to try something new: A "fast enough" linting
engine based on linting code shape, built to be easily extended.

[clj-kondo]: https://github.com/clj-kondo/clj-kondo
[eastwood]: https://github.com/jonase/eastwood
[kibit]: https://github.com/clj-commons/kibit
[clojure-lsp]: https://clojure-lsp.io/
[core.logic]: https://github.com/clojure/core.logic
[rubocop]: https://rubocop.org/

## Non-goals

For speed and simplicity, Splint doesn't run any code, it only works on the provided
code as text. As such, it doesn't understand macros or perform any macro-expansion
(unlike Eastwood) so it can only lint a given macro call, not the resulting code.

clj-kondo performs lexical analysis and can output usage and binding information, such
as unused or incorrectly defined vars. At this time, Splint makes no such efforts. It is
only focused on code shape, not code intent or meaning.

## Speed comparison

```
$ tokei ../netrunner/
===============================================================================
 Language            Files        Lines         Code     Comments       Blanks
===============================================================================
 Clojure               169       113620       102756         4266         6598
 ClojureC                4         1012          850           36          126
 ClojureScript          48        12666        11649          142          875

$ time clojure -M:run ../netrunner
...
Linting took 6943ms, 614 style warnings

real    0m9.673s
user    0m31.007s
sys     0m0.751s

$ clojure -T:build uber
$ time java -jar target/splint-1.0.0-standalone.jar ../netrunner
...
Linting took 6615ms, 614 style warnings

real    0m8.173s
user    0m27.289s
sys     0m0.588s

$ bbin install io.github.noahtheduke/splint
{:coords
 #:git{:url "https://github.com/noahtheduke/splint",
       :tag "v0.1",
       :sha "7061c2c72c575876f6da32b3c146814e25fd84bd"},
 :lib io.github.noahtheduke/splint}
$ time splint ../netrunner/
...
Linting took 16693ms, 614 style warnings

real    0m17.883s
user    0m17.700s
sys     0m0.180s
```

## License

Copyright © Noah Bogart

Distributed under the Mozilla Public License version 2.0.
