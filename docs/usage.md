# Basic Usage

Running `splint` with no arguments or `--help` will print the help:

```text
$ clojure -M:splint
splint: s-expression pattern matching and linting engine
...
```

Pass in any number of files or directories to lint them:

```text
$ clojure -M:splint src/main.clj test/
...
Linting took 7168ms, 696 style warnings
```
