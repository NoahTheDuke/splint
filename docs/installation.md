# Installation and Aliases

Pretty standard installation as a library.

For Clojure CLI:

```clojure
:aliases {:splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "1.7.0"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

And in Leiningen, add this to `project.clj`:

```clojure
:profiles {:dev {:dependencies [io.github.noahtheduke/splint "1.7.0"]}}
:aliases {"splint" ["run" "-m" "noahtheduke.splint"]}
```

## Jar / Native

At some point, I hope to have downloadable versions but I'm still figuring that out.

In the meantime, it runs fast on babashka and can be installed using `bbin`:

```text
$ bbin install io.github.noahtheduke/splint
{:coords
 #:git{:url "https://github.com/noahtheduke/splint",
       :tag "v1.7.0",
       :sha "..."},
 :lib io.github.noahtheduke/splint}
```

## Minimum Clojure version

Splint supports Clojure 1.11+. If you wish to use Splint in a project targeting an earlier version, you'll have to add `org.clojure/clojure {:mvn/version "1.11.1"}` to the `extra-deps` in the alias.
