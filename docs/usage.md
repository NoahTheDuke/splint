# Basic Usage

Running `splint` with no arguments or `--help` will print the help:

```text
$ clojure -M:splint
splint v1.10.1

Usage:
  splint [options] [path...]
  splint [options] -- [path...]
...
```

Pass in any number of files or directories to lint them:

```text
$ clojure -M:splint src/main.clj test/
...
Linting took 7168ms, 696 style warnings
```
