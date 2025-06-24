# Installation and Aliases

When used in a project as a library, put it in an alias to make it easier to invoke.

## Clojure CLI

```clojure
:aliases {:splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "1.21.0"}
                                org.clojure/clojure {:mvn/version "1.11.1"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

Run with `clojure -M:splint [args...]`.

## Leiningen

Add this to `project.clj`:

```clojure
:profiles {:dev {:dependencies [[io.github.noahtheduke/splint "1.21.0"]
                                [org.clojure/clojure "1.11.1"]]}}
:aliases {"splint" ["run" "-m" "noahtheduke.splint"]}
```

Run with `lein splint [args...]`.

## Jar / Native

At some point, I hope to have downloadable versions but I'm still figuring that out.

In the meantime, it runs fast on babashka and can be installed using `bbin`:

```text
$ bbin install io.github.noahtheduke/splint
{:coords
 #:git{:url "https://github.com/noahtheduke/splint",
       :tag "v1.21.0",
       :sha "..."},
 :lib io.github.noahtheduke/splint}
```

## Minimum Clojure version

Splint requires Clojure 1.11+. If you wish to use Splint in a project targeting an earlier version, you'll have to add `org.clojure/clojure {:mvn/version "1.11.1"}` to the `extra-deps` in the alias.
