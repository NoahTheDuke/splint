# Installation and Aliases

Pretty standard installation as a library.

For Clojure CLI:

```clojure
:aliases {:splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "1.2.2"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

And in Leiningen, add this to `project.clj`:

```clojure
:profiles {:dev {:dependencies [io.github.noahtheduke/splint "1.2.2"]}}
:aliases {"splint" ["run" "-m" "noahtheduke.splint"]}
```

## Jar / Native

At some point, I hope to have downloadable versions but I'm still figuring that
out.

In the meantime, it runs fast on babashka and can be installed using `bbin`:

```text
$ bbin install io.github.noahtheduke/splint
{:coords
 #:git{:url "https://github.com/noahtheduke/splint",
       :tag "v1.2.2",
       :sha "..."},
 :lib io.github.noahtheduke/splint}
```
