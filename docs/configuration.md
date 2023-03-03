# Configuration

`splint` has lots of configuration, just lots and lots. They can be defined in a `.splint.edn` file at the root of the project.

The format of the file is like all the other config files in clojureland:

```clojure
{quiet true
 lint/eq-nil {:enabled false}}
```
