# Genre Overview

* `lint` is for code that can be easily incorrect or is suspicious (`if` missing a branch, a threading macro with a single element, etc).
* `metrics` is about properties of code that can be measured, such as length of functions.
* `naming` is about the names given to defining forms (`defn`, `defrecord`, etc) when they don't adhere to Clojure community idioms or style.
* `performance` is for code that has faster alternatives at the cost of larger or more unwieldy code (`(assoc m :k1 v1 :k2 v2)` vs `(-> m (assoc :k1 v1) (assoc :k2 v2))`, etc). These are disabled by default as they are more contentious than the rest.
* `style` is for code that doesn't adhere to Clojure community idioms or style, especially when there are existing alternatives (`(when (not ...))` vs `(when-not ...)`, etc).

**Note**: There is also `dev` used to lint Splint's own codebase that aren't meant to be public. These are stored in the `dev/` folder.
