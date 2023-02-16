# Next-Generation kibit-style linter

Compares forms against known templates, prints the more idiomatic/"Clojure-y" way to
write the form.

Uses macros to generate fast and efficient comparison functions, can use predicates to
match against, can capture matched forms.

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
$ time java -jar target/spat-1.0.0-standalone.jar ../netrunner
...
Linting took 6615ms, 614 style warnings

real    0m8.173s
user    0m27.289s
sys     0m0.588s

$ bbinstall io.github.noahtheduke/spat
{:coords
 #:git{:url "https://github.com/noahtheduke/spat",
       :tag "v0.1",
       :sha "7061c2c72c575876f6da32b3c146814e25fd84bd"},
 :lib io.github.noahtheduke/spat}
$ time spat ../netrunner/
...
Linting took 16693ms, 614 style warnings

real    0m17.883s
user    0m17.700s
sys     0m0.180s
```
## License

Copyright © 2022 Noah Bogart

Distributed under the Mozilla Public License version 2.0.
