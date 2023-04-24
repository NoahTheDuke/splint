; Adapted from Kibit
; Kibit: Copyright © 2012 Jonas Enlund, ELP 1.0
; Modifications licensed under ELP 1.0

(ns noahtheduke.spat.ns-parser)

(defmulti derive-aliases first :default 'ns)

(defn quoted? [form]
  (and (seq? form)
       (= 'quote (first form))))

(defn unquote-if-quoted
  [form]
  (if (quoted? form)
    (second form)
    form))

(defn parse-imports [args]
  (reduce
    (fn [acc cur]
      (cond
        (symbol? cur)
        (assoc acc cur cur)
        (seq? cur)
        (let [prefix (first cur)
              aliases (rest cur)]
          (reduce
            (fn [acc alias_]
              (let [full-name (symbol (str prefix "." alias_))]
                (-> acc
                    (assoc alias_ full-name)
                    (assoc full-name full-name))))
            acc aliases))
        :else acc))
    {} args))

(defmethod derive-aliases 'import
  [[_ & args]]
  {:imports (parse-imports args)})

(defn- prefix-spec?
  "Returns true if form represents a libspec prefix list like
  (prefix name1 name1) or [com.example.prefix [name1 :as name1]]"
  [form]
  (and (sequential? form)  ; should be a list, but often is not
       (symbol? (first form))
       (not-any? keyword? form)
       (< 1 (count form))))  ; not a bare vector like [foo]

(defn- option-spec?
  "Returns true if form represents a libspec vector containing optional
  keyword arguments like [namespace :as alias] or
  [namespace :refer (x y)] or just [namespace]"
  [form]
  (and (sequential? form)  ; should be a vector, but often is not
       (symbol? (first form))
       (or (keyword? (second form))  ; vector like [foo :as f]
           (= 1 (count form)))))  ; bare vector like [foo]

(defn- js-dep-spec?
  "A version of `option-spec?` for native JS dependencies, i.e. vectors
   like [\"react-dom\" :as react-dom] or just [\"some-polyfill\"]"
  [form]
  (and (sequential? form)  ; should be a vector, but often is not
       (string? (first form))
       (or (keyword? (second form))  ; vector like ["foo" :as f]
           (= 1 (count form)))))

(defn- deps-from-libspec
  "A modification from clojure.tools.namespace.parse/deps-from-libspec."
  [prefix form]
  (cond (prefix-spec? form)
        (mapcat (fn [f] (deps-from-libspec
                          (symbol (str (when prefix (str prefix "."))
                                       (first form)))
                          f))
                (next form))

        (option-spec? form)
        (let [opts (apply hash-map (next form))]
          [{:ns (symbol (str (when prefix (str prefix ".")) (first form)))
            :alias (or (:as opts) (:as-alias opts))}])

        (js-dep-spec? form)
        (let [opts (apply hash-map (next form))]
          [{:ns (str (when prefix (str prefix ".")) (first form))
            :alias (or (:as opts) (:as-alias opts))}])))

(defn derive-aliases-from-deps
  "Takes a vector of `deps`, of which each element is in the form accepted by
  the `ns` and `require` functions to specify dependencies. Returns a map where
  each key is a clojure.lang.Symbol that represents the alias, and each value
  is the clojure.lang.Symbol that represents the namespace that the alias refers to."
  [deps]
  (->> deps
       (mapcat #(deps-from-libspec nil (unquote-if-quoted %)))
       (filter :alias)
       (into {} (map (fn [dep] [(:alias dep) (:ns dep)])))))

(defmethod derive-aliases 'require
  [deps]
  {:aliases (derive-aliases-from-deps (next deps))})

(defmethod derive-aliases 'use
  [deps]
  {:aliases (derive-aliases-from-deps (next deps))})

(defmethod derive-aliases 'ns
  [[_ ns_ & references]]
  (let [libspecs (->> references
                      (filter sequential?)
                      (group-by #(-> % first name keyword)))]
   {:current (unquote-if-quoted ns_)
    :aliases (->> libspecs
                  ((juxt :require :require-macros :use))
                  (apply concat)
                  (keep #(-> % next derive-aliases-from-deps))
                  (apply merge))
    :imports (->> (:import libspecs)
                  (map parse-imports)
                  (apply merge-with into))}))

(defmethod derive-aliases 'in-ns
  [[_ ns_]]
  {:current (unquote-if-quoted ns_)})

(defmethod derive-aliases 'alias
  [[_ alias namespace-sym]]
  (when (and (quoted? alias) (quoted? namespace-sym))
    {:aliases {(second alias) (second namespace-sym)}}))

(defmethod derive-aliases 'refer
  [_args]
  nil)

(defmethod derive-aliases 'refer-clojure
  [_args]
  nil)

(defn parse-ns [obj]
  (when (list? obj)
    (case (first obj)
      (alias import in-ns ns require use) (derive-aliases obj)
      ; else
      nil)))
