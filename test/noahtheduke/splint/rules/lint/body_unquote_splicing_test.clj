; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.lint.body-unquote-splicing-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-alt]]))

(defexpect only-body-test
  (doseq [input '[delay dosync future lazy-cat lazy-seq pvalues
                  with-loading-context]]
    (expect (list input '(let [res# (do (splint/unquote-splicing body))] res#))
      (check-alt (format "(%s ~@body)" input)))))

(defexpect init-arg-test
  (doseq [input '[binding locking sync with-bindings with-in-str
                  with-local-vars with-precision with-redefs]]
    (expect (list input 'arg '(let [res# (do (splint/unquote-splicing body))] res#))
      (check-alt (format "(%s arg ~@body)" input)))))