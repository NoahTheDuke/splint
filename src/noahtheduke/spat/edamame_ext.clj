(ns noahtheduke.spat.edamame-ext
  "Remove once Babashka releases with support for clojure.lang.MapEntry/create"
  (:require
    [edamame.impl.read-fn :as read-fn]))

(alter-var-root
  #'read-fn/walk*
  (fn [_]
    (fn
      [inner outer form]
      (cond
        (list? form) (with-meta (outer (apply list (map inner form)))
                                (meta form))
        (instance? clojure.lang.IMapEntry form)
        (outer (clojure.lang.MapEntry. (inner (key form)) (inner (val form))))
        (seq? form) (with-meta (outer (doall (map inner form)))
                               (meta form))
        (instance? clojure.lang.IRecord form)
        (outer (reduce (fn [r x] (conj r (inner x))) form form))
        (coll? form) (outer (into (empty form) (map inner form)))
        :else (outer form)))))
