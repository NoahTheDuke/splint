; Adapted from Clojure
; Clojure: Copyright (c) Rich Hickey. All rights reserved., ELP 1.0
; Modifications licensed under ELP 1.0

(ns noahtheduke.splint.parser.defn
  (:require
    [noahtheduke.splint.utils :refer [drop-quote]]))

(set! *warn-on-reflection* true)

(defn- sigs
  "Adapted from clojure.core/sigs"
  [fdecl]
  (let [asig
        (fn [fdecl]
          (let [arglist (first fdecl)
                ;elide implicit macro args
                arglist (if (= '&form (first arglist))
                          (subvec arglist 2 (count arglist))
                          arglist)
                body (next fdecl)]
            (if (map? (first body))
              (if (next body)
                (with-meta arglist (conj (or (meta arglist) {}) (first body)))
                arglist)
              arglist)))]
    (if (seq? (first fdecl))
      (loop [ret [] fdecls fdecl]
        (if fdecls
          (recur (conj ret (asig (first fdecls))) (next fdecls))
          (seq ret)))
      (list (asig fdecl)))))

(defn parse-defn
  "Adapted from clojure.core but returns `nil` if given an improperly formed defn."
  [form]
  (let [fdecl (next form)
        fname (when (symbol? (first fdecl)) (first fdecl))]
    (when fname
      (let [fdecl (next fdecl)
            m {:splint/name fname}
            ; docstring
            m (if (string? (first fdecl))
                (assoc m :doc (first fdecl))
                m)
            fdecl (if (string? (first fdecl))
                    (next fdecl)
                    fdecl)
            ; pre attr-map
            m (if (map? (first fdecl))
                (conj m (first fdecl))
                m)
            fdecl (if (map? (first fdecl))
                    (next fdecl)
                    fdecl)
            ;; function bodies
            fdecl (cond
                    ;; For linting purposes, it's helpful to track the location
                    ;; of function "arities" (the arg vector plus fn body). If
                    ;; the function has single or multiple arities but they're
                    ;; all wrapped in lists, then they'll have location data
                    ;; already. However, if it's a single arity function, the
                    ;; "arity" won't have location data as it's a plain seq
                    ;; built from calling `next` repeatedly. Therefore, we
                    ;; gotta do it ourselves here.
                    ;;
                    ;; At this point, fdecl is either:
                    ;; - a single arity: ([] 1 2 3)
                    ;; - a single arity wrapped in a list: (([] 1 2 3))
                    ;; - multiple arities (each wrapped in a list): (([] 1 2 3) ([a] a 1 2 3))
                    ;; If it's the first, that means when we create the wrapped
                    ;; version, we don't carry forward the metadata of the
                    ;; "body" (arglist plus actual body).
                    ;;
                    ;; To do that, we have to convert the fdecl seq to
                    ;; a concrete list, and attach to it the position of the
                    ;; vector at the start and one less than the position of
                    ;; the function's form at the end (because ends are
                    ;; exclusive indices).
                    ;;         start   end
                    ;;           v      v
                    ;; (defn foo [] 1 2 3)
                    (vector? (first fdecl))
                    (let [vm (meta (first fdecl))
                          loc {:line (:line vm)
                               :column (:column vm)
                               :end-row (:end-row (meta form))
                               :end-col (dec (:end-col (meta form)))}]
                      (-> (apply list fdecl)
                          (vary-meta (fnil conj {}) loc)
                          (list)))
                    ;; Otherwise, just use the existing list (which will have
                    ;; location data already).
                    (and (list? (first fdecl))
                         (every? #(vector? (first %)) fdecl)) fdecl
                    ;; Explicitly, if given a faulty defn form, kick out
                    :else nil)]
        (when fdecl
          (let [; post-attr-map
                m (if (map? (last fdecl))
                    (conj m (last fdecl))
                    m)
                fdecl (if (map? (last fdecl))
                        (apply list (butlast fdecl))
                        fdecl)
                m (assoc m :arities fdecl)
                m (when fdecl
                    (if (contains? m :arglists)
                      (update m :arglists drop-quote)
                      (assoc m :arglists (sigs fdecl))))
                m (when m
                    (conj (or (meta fname) {}) m))]
            m))))))
