# Installation and Aliases

Pretty standard installation as a library.

For Clojure CLI:

```
:aliases {:splint {:extra-deps {noahtheduke/spat {:mvn/version "some version"}}
                   :main-opts ["-m" "noahtheduke.splint"]}}
```

And in Leiningen, add this to `project.clj`:

```
:profiles {:dev {:dependencies [noahtheduke/splint "some version"]}}
:aliases {"splint" ["run" "-m" "noahtheduke.splint"]}
```

## Jar / Native

At some point, I hope to have downloadable versions but I'm still figuring that
out.
