; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runners.autocorrect 
  (:require
   [clojure.string :as str]
   [edamame.impl.read-fn :as read-fn]
   [noahtheduke.splint.clojure-ext.core :refer [with-meta*]]
   [noahtheduke.splint.printer :as printer]
   [noahtheduke.splint.runner :as run]
   [noahtheduke.splint.utils :refer [drop-quote simple-type]]
   [rewrite-clj.zip :as zip]) 
  (:import
   [java.io File]))

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

(defn prep-form [ctx zloc]
  (let [tag (zip/tag zloc)
        form (zip/sexpr zloc)
        form (case tag
               :deref (cons 'splint/deref (next form))
               :eval (cons 'splint/read-eval (drop-quote (next form)))
               :forms (vec (zip/child-sexprs zloc))
               :fn (cons 'splint/fn (read-fn/read-fn (next form)))
               :quote (cons 'splint/quote (next form))
               :reader-macro (let [tag-meta {:ext (:ext ctx)}
                                   tag (vary-meta 'splint/tagged-literal merge tag-meta)
                                   zloc (zip/next zloc)
                                   r (zip/sexpr zloc)
                                   zloc (zip/next zloc)
                                   v (zip/sexpr zloc)]
                               {tag (list r v)})
               :syntax-quote (cons 'splint/syntax-quote (next form))
               :unquote (cons 'splint/unquote (next form))
               :unquote-splicing (cons 'splint/unquote-splicing (next form))
               :var (cons 'splint/var (next form))
               #_:else form)
        [[line column] [end-line end-column]] (zip/position-span zloc)]
    (with-meta* form {:line line
                      :column column
                      :end-line end-line
                      :end-column end-column})))

(comment
  (->> "#'(+ 1 %)"
       zip/of-string
       (prep-form {:ext :clj})
       second
       ))

(defn check-form
  [ctx rule-names zloc]
  (reduce
   (fn [zloc rule-name]
     (let [rule (-> ctx :rules rule-name)]
       (if (-> rule :config :enabled)
         (try
           (let [form (prep-form ctx zloc)
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
                                     (let [alt (:alt diagnostic)]
                                       (if (meta alt)
                                         (vary-meta alt dissoc :line :column)
                                         alt))))]
                        (or (zip/left zloc)
                            (zip/up zloc)
                            zloc)))
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
   rule-names))

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
          m (zip/sexpr zloc)
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
  (let [[ctx zloc] (update-rules ctx zloc)
        form-type (simple-type-for-zloc zloc)
        zloc (zip/next
              (if-let [rules-for-type (-> ctx :rules-by-type form-type not-empty)]
                (check-form ctx rules-for-type zloc)
                zloc))]
    (if (zip/end? zloc)
      zloc
      (let [form (zip/sexpr zloc)
            ctx (assoc ctx :parent-form form)]
        (if (and (= :list (simple-type form))
                 (#{'quote 'splint/quote} (first form)))
          (walk ctx (or (zip/right zloc)
                        (zip/next zloc)))
          (walk ctx zloc))))))

(defn check-files
  [ctx files]
  (doseq [file-obj files
          :let [{:keys [ext ^File file contents]} (run/slurp-file file-obj)]]
    (try
      (let [zloc (zip/of-string* contents {:track-position? true})
            ctx (-> ctx
                    (update :checked-files swap! conj file)
                    (assoc :ext ext)
                    (assoc :filename file)
                    (assoc :file-str contents)
                    (run/pre-filter-rules))
            zloc (walk ctx zloc)]
        (if (.exists file)
          (spit file (zip/root-string zloc))
          (do (println "\nFull file:")
              (println (zip/root-string zloc))))
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
  (zip/root (zip/root (zip/of-string "a")))
  (def zloc (zip/of-string* "(ns hello) (+ 1 x)"))
  (-> (zip/of-string* "{:a :b}")
      (zip/next*)
      (zip/next*)
      ; (zip/tag)
      #_(zip/next*)
      #_(zip/sexpr))
  (let [
        config {:clojure-version *clojure-version*
                :parallel false
                :autocorrect true}
        ctx (run/prepare-context config)
        paths ["(ns hello) (+ 1 x)"]
        files (run/resolve-files-from-paths ctx paths)
        results (check-files ctx files)]
    (prn files results)
    ))
