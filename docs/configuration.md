# Configuration

`splint` has lots of configuration, just lots and lots. They can be defined in a `.splint.edn` file at the root of the project.

The format of the file is that any of the cli options can be added here, and all rules must be fully-qualified with their settings in a map as the key:

```clojure
{quiet true
 lint/eq-nil {:enabled false}}
```

Inline, a single rule can be disabled in the following form with `#_:splint/disable`:

```clojure
#_:splint/disable (+ 1 x)
```

Entire genres of rules and specific rules can be disabled in the same manner, by using the map form `#_{:splint/disable [style lint/plus-one]}` will disable all `style` rules and the specific `lint/plus-one` rule in the following form.
