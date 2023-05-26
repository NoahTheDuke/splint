(ns arglists)

(defn normal [a] a)

(defn docstrings
  "This is a docstring"
  [a] a)

(defn pre-attr-map
  {:arg 1}
  [a] a)

(defn post-attr-map
  ([a] a)
  {:arg 1})

(defn rest-args
  [a b & c]
  (apply + a b c))

(defn rest-multiple-bodies
  ([a b] (+ a b))
  ([a b & c]
   (apply + a b c)))

(defn destructuring
  [{:keys [a b c]}]
  (+ a b c))

(defn wrapped-body ([a] a))

(defn multiple-bodies ([a] a) ([a b] (+ a b)))

(defn arglist-metadata
  {:arglists '([a] [a b] [a b c])}
  [& args] (apply + args))
