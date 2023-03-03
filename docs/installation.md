# Installation and Aliases

Pretty standard installation as a library.

For Clojure CLI:

```
:aliases {:spat {:extra-deps {noahtheduke/spat {:mvn/version "some version"}}
                 :main-opts ["-m" "noahtheduke.spat"]}}
```

And in Leiningen, add this to `project.clj`:

```
:profiles {:dev {:dependencies [noahtheduke/spat "some version"]}}
:aliases {"spat" ["run" "-m" "noahtheduke.spat"]}
```

## Jar / Native

At some point, I hope to have downloadable versions but I'm still figuring that
out.
