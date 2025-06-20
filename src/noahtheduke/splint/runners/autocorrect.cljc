; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runners.autocorrect 
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   clojure.tools.reader.reader-types
   [edamame.impl.read-fn :as read-fn]
   [noahtheduke.splint.clojure-ext.core :refer [->list parse-map parse-set
                                                postwalk* with-meta*]]
   [noahtheduke.splint.printer :as printer :refer [*fipp-width* pprint-str]]
   [noahtheduke.splint.runner :as run]
   [noahtheduke.splint.utils :refer [simple-type]]
   [rewrite-clj.node :as node]
   [rewrite-clj.zip :as zip])
  (:import
   [java.io File]
   [noahtheduke.splint.clojure_ext.core ParseMap ParseSet]))

(set! *warn-on-reflection* true)

(defn prompt
  "Create a yes/no prompt using the given message.

  From `leiningen.ancient.console`."
  [& msg]
  (let [msg (str (str/join msg) " [yes/no] ")]
    (locking *out*
      (loop [i 0]
        (when (= (mod i 4) 2)
          (println "*** please type in one of 'yes'/'y' or 'no'/'n' ***"))
        (print msg)
        (flush)
        (let [r (str/lower-case (or (read-line) ""))]
          (case r
            ("yes" "y") true
            ("no" "n")  false
            (recur (inc i))))))))

(defn report-or-prompt
  [ctx {:keys [form alt filename line]}]
  (if (-> ctx :config :interactive)
    (prompt (with-out-str
              (println "Would you like to replace")
              (printer/print-form form)
              (println " with")
              (printer/print-form alt)
              (print (format "in %s:%s?" filename line))))
    (do
      (println "Replacing")
      (printer/print-form form)
      (println " with")
      (printer/print-form alt)
      (println (format "in %s:%s" filename line))
      (newline)
      (flush)
      true)))

;; from edamame.impl.parser
(defn desugar-meta
  "Resolves syntactical sugar in metadata" ;; could be combined with some other desugar?
  [f]
  (cond
    (keyword? f) {f true}
    (symbol? f)  {:tag f}
    (string? f)  {:tag f}
    (vector? f)  {:param-tags f}
    :else        f))

(defn node->sexpr [ctx node]
  (let [tag (node/tag node)]
    (if-not (node/inner? node)
      (case tag
        :regex (list 'splint/re-pattern node)
        #_:else (node/sexpr node))
      (let [children (->> (node/children node)
                          (remove node/printable-only?)
                          (map #(node->sexpr ctx %))
                          (remove #{::missing-reader-cond}))]
        (case tag
          :deref (cons 'splint/deref children)
          :eval (cons 'splint/read-eval children)
          :forms (vec children)
          :fn (->list (cons 'splint/fn (next (read-fn/read-fn children))))
          :list (->list children)
          :map (parse-map (ParseMap. children)
                          (set/rename-keys (meta node)
                                           {:row :line
                                            :col :column}))
          :namespaced-map (second children)
          :meta (let [[m obj] children]
                  (vary-meta obj merge (desugar-meta m)))
          :quote (cons 'quote children)
          :reader-macro
          (if (= '? (first children))
            (let [branches (apply hash-map (fnext children))]
              (get branches (:ext ctx ::missing-reader-cond)))
            (let [tag-meta {:ext (:ext ctx)}
                  tag (vary-meta 'splint/tagged-literal merge tag-meta)]
              {tag (->list children)}))
          :set (parse-set (ParseSet. children)
                          (set/rename-keys (meta node)
                                           {:row :line
                                            :col :column}))
          :syntax-quote (cons 'splint/syntax-quote children)
          :uneval nil
          :unquote (cons 'splint/unquote children)
          :unquote-splicing (cons 'splint/unquote-splicing children)
          :var (cons 'splint/var children)
          :vector (vec children)
          #_:else (throw (ex-info "oops" {:node node :tag tag})))))))

(defn prep-form [ctx zloc]
  (let [form (node->sexpr ctx (zip/node zloc))
        [[line column] [end-line end-column]] (zip/position-span zloc)]
    (with-meta* form {:line line
                      :column column
                      :end-line end-line
                      :end-column end-column})))

(defn sexpr->node [alt]
  (postwalk*
   (fn [obj]
     (if (and (sequential? obj) (not (vector? obj)))
       (case (first obj)
         splint/deref (node/deref-node (next obj))
         splint/fn (node/fn-node
                    (->> (repeat (node/whitespace-node " "))
                         (interleave (nth obj 2))
                         (butlast)))
         (quote splint/quote) (node/quote-node (next obj))
         splint/re-pattern (node/regex-node (next obj))
         splint/read-eval (node/eval-node (next obj))
         splint/syntax-quote (node/syntax-quote-node (next obj))
         splint/unquote (node/unquote-node (next obj))
         splint/unquote-splicing (node/unquote-splicing-node (next obj))
         splint/var (node/var-node (next obj))
         #_:else obj)
       obj))
   alt))

(defn get-lines-from-alt [column alt]
  (let [width (max 40 (- *fipp-width* column))]
    (str/split
      (binding [*fipp-width* width]
        (pprint-str alt))
      #",?\r?\n")))

(defn check-form
  [ctx rule-names zloc]
  (let [form-cache (volatile! nil)]
    (reduce
     (fn [zloc rule-name]
       (let [rule (-> ctx :rules rule-name)]
         (if (-> rule :config :enabled)
           (try
             (let [form (or @form-cache
                            (vreset! form-cache (prep-form ctx zloc)))
                   diagnostic (run/check-rule ctx rule form)]
               (cond
                 (not diagnostic) zloc
                 (sequential? diagnostic)
                 (do #_{:clj-kondo/ignore [:unused-value]}
                     (update ctx :diagnostics swap! into diagnostic)
                     zloc)
                 :else
                 (do #_{:clj-kondo/ignore [:unused-value]}
                     (update ctx :diagnostics swap! conj diagnostic)
                     (if (and (-> ctx :config :autocorrect)
                              (:autocorrect rule)
                              (report-or-prompt ctx diagnostic))
                       (reduced
                        (let [zloc (zip/edit zloc
                                     (fn -replace-zipper [_]
                                       (let [[_line column] (zip/position zloc)
                                             [leading & lines] (get-lines-from-alt column (:alt diagnostic))]
                                         (->> lines
                                              (map #(str (str/join (repeat (dec column) " ")) %))
                                              (str/join "\n")
                                              (str leading "\n")
                                              (zip/of-string)
                                              (zip/node)))))]
                          (vreset! form-cache
                                   (or (zip/left zloc)
                                       (zip/up zloc)
                                       zloc))))
                       zloc))))
             (catch Exception ex
               (update ctx :diagnostics swap! conj
                       (run/runner-error->diagnostic
                        ex {:error-name 'splint/error
                            :form zloc
                            :rule-name (:full-name rule)
                            :filename (:filename ctx)}))
               zloc))
           zloc)))
     zloc
     rule-names)))

(defn simple-type-for-zloc
  "rewrite-clj has different types than splint, so we must bridge them."
  [zloc]
  (let [tag (zip/tag zloc)]
    (case tag
      :forms :file
      (:eval :fn :quote :reader-macro :syntax-quote) :list
      ;; TODO (2024-09-04): add :regex to splint.pattern
      :regex 'java.util.regex.Pattern
      :token (simple-type (zip/sexpr zloc))
      #_:else tag)))

(comment
  (simple-type-for-zloc
   (-> (zip/of-string* "(quote asdf)")
       (zip/right))))

(defn update-rules
  [ctx zloc]
  (if (= :uneval (zip/tag zloc))
    (let [zloc (zip/next zloc)
          m (node->sexpr ctx (zip/node zloc))
          zloc (zip/next zloc)]
      (cond
        (= :splint/disable m)
        [(update ctx :rules run/update-rules (with-meta [] {:splint/disable true}))
         zloc]
        (:splint/disable m)
        [(update ctx :rules run/update-rules (with-meta [] {:splint/disable (:splint/disable m)}))
         zloc]
        :else
        [ctx zloc]))
    [ctx zloc]))

(defn walk
  "Check a given form and then map recur over each of the form's children."
  [ctx zloc]
  (if (zip/end? zloc)
    zloc
    (let [[ctx zloc] (update-rules ctx zloc)
          form-type (simple-type-for-zloc zloc)
          form (node->sexpr ctx (zip/node zloc))
          parent-form (when-let [parent (zip/up zloc)]
                        (node->sexpr ctx (zip/node parent)))
          ctx (assoc ctx :parent-form parent-form)]
      (if (and (= :list form-type) (= 'quote (first form)))
        (let [zloc (loop [p zloc]
                     (if-let [loc (zip/up p)]
                       (or (zip/right loc)
                           (recur loc))
                       (assoc p :end? true)))]
          (recur ctx zloc))
        (recur ctx
               (zip/next
                (if-let [rules-for-type (-> ctx :rules-by-type form-type not-empty)]
                  (check-form ctx rules-for-type zloc)
                  zloc)))))))

(defn check-files
  [ctx files]
  (doseq [file-obj files
          :let [{:keys [ext ^File file contents]} (run/slurp-file file-obj)]]
    (try
      (swap! (:checked-files ctx) conj file)
      (let [zloc (zip/of-string* contents {:track-position? true})
            ctx (-> ctx
                    (assoc :ext ext)
                    (assoc :filename file)
                    (assoc :file-str contents)
                    (run/pre-filter-rules))
            zloc (walk ctx zloc)]
        (when (.exists file)
          (when-let [new-contents (not-empty (zip/root-string zloc))]
            (spit file new-contents)))
        nil)
      (catch Exception ex
        (let [data (ex-data ex)]
          (if (= :edamame/error (:type data))
            (let [data (-> data
                           (assoc :error-name 'splint/parsing-error)
                           (assoc :filename file)
                           (assoc :form-meta {:line (:line data)
                                              :column (:column data)}))
                  diagnostic (run/runner-error->diagnostic ex data)]
              (update ctx :diagnostics swap! conj diagnostic))
            (let [diagnostic (run/runner-error->diagnostic
                              ex {:error-name 'splint/unknown-error
                                  :filename file})]
              (update ctx :diagnostics swap! conj diagnostic))))))))

(comment
  (let [config {:clojure-version *clojure-version*
                :parallel false
                :autocorrect true}
        ctx (run/prepare-context config)
        paths ["'[{:form (assoc-in coll [:k] v)
         :message \"Use `assoc` instead of recreating it.\"
         :alt (assoc coll :k v)}]"]
        files (run/resolve-files-from-paths ctx paths)]
    (check-files ctx files)))
