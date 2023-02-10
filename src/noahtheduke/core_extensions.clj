;; EPL same as Clojure Core

(ns noahtheduke.core-extensions)

(defn meta-preserving-walk
  "Same as clojure.walk/walk but preserves meta."
  [inner outer form]
  (cond
   (list? form)
   (with-meta (outer (apply list (map inner form)))
              (meta form))
   (instance? clojure.lang.IMapEntry form)
   (outer (clojure.lang.MapEntry/create (inner (key form))
                                        (inner (val form))))
   (seq? form)
   (with-meta (outer (doall (map inner form)))
              (meta form))
   (record? form)
   (outer (reduce (fn [r x] (conj r (inner x))) form form))
   (coll? form)
   (outer (into (empty form) (map inner form)))
   :else
   (outer form)))

(defn postwalk*
  "Same as clojure.walk/postwalk but uses meta-preserving-walk."
  [f form]
  (meta-preserving-walk #(postwalk* f %) f form))

(defn postwalk-replace*
  "Same as clojure.walk/postwalk-replace but uses meta-preserving-walk."
  [smap form]
  (postwalk* (fn [x] (if (contains? smap x) (smap x) x)) form))
