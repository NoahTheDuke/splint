# Installation and Aliases

When used in a project as a library, put it in an alias to make it easier to invoke.

## Clojure CLI

```clojure
:aliases {:splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "1.23.0"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

Run with `clojure -M:splint [args...]`.

## Leiningen

Add this to `project.clj`:

```clojure
:profiles {:dev {:dependencies [[io.github.noahtheduke/splint "1.23.0"]]}}
:aliases {"splint" ["run" "-m" "noahtheduke.splint"]}
```

Run with `lein splint [args...]`.

## Babashka

Requires version 1.12.205 or later. If using `bb.edn`, add this to `bb.edn`:

```clojure
:tasks {splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "1.23.0"}}
                :task noahtheduke.splint/-main}}
```

Run with `bb splint [args...]`.

It can also be installed using `bbin`:

```text
$ bbin install io.github.noahtheduke/splint
{:coords
 #:git{:url "https://github.com/noahtheduke/splint",
       :tag "v1.23.0",
       :sha "..."},
 :lib io.github.noahtheduke/splint}
```

Run with `splint [args...]`.
