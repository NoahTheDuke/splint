(ns noahtheduke.spat.rules.helpers)

(defn symbol-or-keyword-or-list? [node]
  (or (symbol? node)
      (keyword? node)
      (list? node)
      (and (sequential? node) (not (vector? node)))))
