; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.body-unquote-splicing-test
  (:require
   [lazytest.core :refer [defdescribe it]]
   [noahtheduke.splint.test-helpers :refer [expect-match single-rule-config]]))

(set! *warn-on-reflection* true)

(defn config [] (single-rule-config 'lint/body-unquote-splicing))

(defdescribe body-unquote-splicing-test

  (it "respects bare ~@"
    (doseq [input '[delay dosync future lazy-cat lazy-seq pvalues
                    with-loading-context]]
      (expect-match
        [{:form (list input '(splint/unquote-splicing body))
          :message "Wrap ~@/unquote-splicing in `(let [res# (do ...)] res#)` to avoid unhygenic macro expansion."
          :alt (list input '(let [res# (do (splint/unquote-splicing body))] res#))}]
        (format "(%s ~@body)" input)
        (config))))

  (it "respects built-in macros"
    (doseq [input '[binding locking sync with-bindings with-in-str
                    with-local-vars with-precision with-redefs]]
      (expect-match
        [{:form (list input 'arg '(splint/unquote-splicing body))
          :message "Wrap ~@/unquote-splicing in `(let [res# (do ...)] res#)` to avoid unhygenic macro expansion."
          :alt (list input 'arg '(let [res# (do (splint/unquote-splicing body))] res#))}]
        (format "(%s arg ~@body)" input)
        (config))))

  (it "doesn't touch non-symbols"
    (expect-match
      nil
      "(future ~@(map inc body))"
      (config))
    (expect-match
      nil
      "(future ~@[body])"
      (config))))
