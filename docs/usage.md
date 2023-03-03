# Basic Usage

Running `splint` with no arguments or `--help` will print the help:

```
$ clojure -M:spat
splint: s-expression pattern matching and linting engine
...
```

Pass in any number of files or directories to lint them:

```
$ clojure -M:spat src/main.clj test/
```
