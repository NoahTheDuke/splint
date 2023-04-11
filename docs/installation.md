# Installation and Aliases

Pretty standard installation as a library.

For Clojure CLI:

```clojure
:aliases {:splint {:extra-deps {io.github.noahtheduke/splint {:mvn/version "some version"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

And in Leiningen, add this to `project.clj`:

```clojure
:profiles {:dev {:dependencies [io.github.noahtheduke/splint "some version"]}}
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
       :tag "v0.1.69",
       :sha "019d9b9a9606c7603c819ffe383d23c741682fa1"},
 :lib io.github.noahtheduke/splint}
```
