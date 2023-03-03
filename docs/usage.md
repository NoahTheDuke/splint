# Basic Usage

Running `splint` with no arguments or `--help` will print the help:

```
$ clojure -M:splint
splint: s-expression pattern matching and linting engine
...
```

Pass in any number of files or directories to lint them:

```
$ clojure -M:splint src/main.clj test/
```
