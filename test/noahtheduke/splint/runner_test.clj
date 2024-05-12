; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [expectations.clojure.test :refer [defexpect expect]]
   [matcher-combinators.test :refer [match?]]
   [noahtheduke.splint.dev :as dev]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.runner :as sut]
   [noahtheduke.splint.test-helpers :refer [expect-match print-to-file!
                                            with-temp-files]]))

(set! *warn-on-reflection* true)

(defexpect ignore-rules-test
  (expect-match nil "#_:splint/disable (+ 1 x)"))

(defexpect ignore-genre-test
  (expect-match nil "#_{:splint/disable [style]} (+ 1 x)"))

(defexpect ignore-specific-rule-test
  (expect-match nil "#_{:splint/disable [style/plus-one]} (+ 1 x)"))

(defexpect ignore-unnecessary-rule-test
  (expect-match
    '[{:rule-name style/plus-one}]
    "#_{:splint/disable [style/plus-zero]} (+ 1 x)"))

(defexpect throws-test
  (expect-match
    [{:rule-name 'splint/error
      :form '(very-special-symbol :do-not-match)
      :message "Splint encountered an error during 'dev/throws-on-match: matched"
      :line 1
      :column 1
      :end-line 1
      :end-column 36
      :filename (io/file "example.clj")
      :exception {:cause "matched"
                  :data {:extra :data}
                  :via [{:type 'clojure.lang.ExceptionInfo
                         :message "matched"
                         :data {:extra :data}}]}}]
    "(very-special-symbol :do-not-match)")
  (expect-match
    [{:rule-name 'naming/single-segment-namespace
      :form '(ns throw-in-middle)
      :message "throw-in-middle is a single segment. Consider adding an additional segment."
      :alt nil
      :line 5
      :column 1
      :end-line 5
      :end-column 21
      :filename (io/file "corpus/throw_in_middle.clj")
      :exception nil}
     {:rule-name 'splint/error
      :form '(very-special-symbol :do-not-match)
      :message "Splint encountered an error during 'dev/throws-on-match: matched"
      :alt nil
      :line 7
      :column 1
      :end-line 7
      :end-column 36
      :filename (io/file "corpus" "throw_in_middle.clj")
      :exception {:cause "matched"
                  :data {:extra :data}
                  :via [{:type 'clojure.lang.ExceptionInfo
                         :message "matched"
                         :data {:extra :data}}]}}
     {:rule-name 'lint/let-if
      :form '(let [a 1] (if a (+ a a) 2))
      :message "Use `if-let` instead of recreating it."
      :alt '(if-let [a 1] (+ a a) 2)
      :line 9
      :column 1
      :end-column 29
      :end-line 9
      :filename (io/file "corpus/throw_in_middle.clj")
      :exception nil}]
    (io/file "corpus" "throw_in_middle.clj")))

(defexpect parse-error-test
  (expect-match
    [{:rule-name 'splint/parsing-error
      :form nil
      :message "Splint encountered an error: Map literal contains duplicate key: :a"
      :alt nil
      :line 5
      :column 1
      :end-line nil
      :end-column nil
      :filename (io/file "corpus/parse_error.clj")
      :exception {:cause "Map literal contains duplicate key: :a"
                  :data {:type :edamame/error
                          :line 5
                          :column 1}
                  :via [{:type 'clojure.lang.ExceptionInfo
                         :message "Map literal contains duplicate key: :a"
                         :data {:type :edamame/error
                                :line 5
                                :column 1}}]}}]
    (io/file "corpus" "parse_error.clj")))

(defexpect prepare-rules-test
  (let [config (into {} (select-keys @dev/dev-config
                          ['lint/if-else-nil
                           'naming/lisp-case
                           'lint/warn-on-reflection]))
        rules (into {} (select-keys (:rules @global-rules)
                         ['lint/if-else-nil
                          'naming/lisp-case
                          'lint/warn-on-reflection]))]
    (expect
      (match?
        {:rules {'lint/if-else-nil {:full-name 'lint/if-else-nil}
                 'naming/lisp-case {:full-name 'naming/lisp-case}
                 'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection}}
         :rules-by-type
         {:list ['lint/if-else-nil
                 'naming/lisp-case]
          :file ['lint/warn-on-reflection]}}
        (sut/prepare-rules config rules)))))

(defexpect update-rules-test
  (let [config (into {} (select-keys @dev/dev-config
                          ['lint/if-else-nil
                           'naming/lisp-case
                           'lint/warn-on-reflection]))
        config (update-vals config #(assoc % :enabled true))
        rules (into {} (select-keys (:rules @global-rules)
                         ['lint/if-else-nil
                          'naming/lisp-case
                          'lint/warn-on-reflection]))
        rules (:rules (sut/prepare-rules config rules))]
    (expect rules (sut/update-rules rules nil))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled false}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled false}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^:splint/disable [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled false}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled true}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^{:splint/disable ['lint]} [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled false}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled false}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^{:splint/disable ['lint 'naming]} [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled true}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled true}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^{:splint/disable ['lint/warn-on-reflection]} [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled true}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled false}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^{:splint/disable ['naming/lisp-case
                                                   'lint/warn-on-reflection]} [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled true}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled false}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled false}}}
        (sut/update-rules rules ^{:splint/disable ['naming
                                                   'lint/warn-on-reflection]} [])))
    (expect
      (match?
        {'lint/if-else-nil {:full-name 'lint/if-else-nil
                            :config {:enabled true}}
         'naming/lisp-case {:full-name 'naming/lisp-case
                            :config {:enabled true}}
         'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                   :config {:enabled true}}}
        (sut/update-rules rules ^{:splint/disable ['asdf]} [])))))

(defexpect require-files-test
  (with-temp-files [test-rule "test_rule.clj"
                    test-file "test_file.clj"]
    (print-to-file!
      test-rule
      "(ns test-rule
         (:require
           [noahtheduke.splint.rules :refer [defrule]]))

      (defrule dev/eq-1-1
        \"docstring\"
        {:pattern '(= 1 1)
         :message \"matched\"
         :replace '(= 2 2)})")
    (print-to-file! test-file "(= 1 1)")
    (let [existing-rules @global-rules
          options {:required-files [(str test-rule)]
                   :clojure-version *clojure-version*}
          results (sut/run-impl [test-file] options)]
      (expect (match? [{:rule-name 'dev/eq-1-1
                        :form '(= 1 1)
                        :message "matched"
                        :alt '(= 2 2)
                        :filename test-file}]
                (seq (:diagnostics results))))
      (expect
        (match? {:rules {'dev/eq-1-1 {:genre "dev"
                                      :name "eq-1-1"}}}
          @global-rules))
      (reset! global-rules existing-rules))))

(defexpect auto-gen-config-test
  (with-redefs [spit (fn [file content]
                       {:file file
                        :content content})]
    (expect
      (match?
        {:file ".splint.edn"
         :content
         (str/join
           "\n"
           [(format ";; Splint configuration auto-generated on %s."
              (.format (java.text.SimpleDateFormat. "yyyy-MM-dd")
                (java.util.Date.)))
            ";; All failing rules have been disabled and can be enabled as time allows."
            ""
            "{"
            " ;; Diagnostics count: 1"
            " ;; Always set *warn-on-reflection* to avoid reflection in interop."
            " lint/warn-on-reflection {:enabled false}"
            ""
            " ;; Diagnostics count: 1"
            " ;; Avoid single-segment namespaces."
            " naming/single-segment-namespace {:enabled false}"
            ""
            " ;; Diagnostics count: 1"
            " ;; Prefer `not=` to `(not (= x y))`."
            " style/not-eq {:enabled false}"
            ""
            " ;; Diagnostics count: 1"
            " ;; `when` has an implicit `do`."
            " style/when-do {:enabled false}"
            ""
            " ;; Diagnostics count: 1"
            " ;; Prefer `when-not` to `(when (not x) ...)`."
            " style/when-not-call {:enabled false}"
            "}"])}
        (sut/auto-gen-config [(io/file "corpus" "printer_test.clj")] {:clojure-version {:major 1 :minor 11}})))))
