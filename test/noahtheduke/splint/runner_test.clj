; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe expect it given]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.dev :as dev]
   [noahtheduke.splint.rules :refer [global-rules]]
   [noahtheduke.splint.runner :as sut]
   [noahtheduke.splint.test-helpers :refer [expect-match print-to-file!
                                            with-temp-files with-out-str-data-map]]))

(set! *warn-on-reflection* true)

(defdescribe ignore-test
  (it "full ingore"
    (expect-match nil "#_:splint/disable (+ 1 x)"))
  (it "ignore genre"
    (expect-match nil "#_{:splint/disable [style]} (+ 1 x)"))
  (it "ignore specific rule"
    (expect-match nil "#_{:splint/disable [style/plus-one]} (+ 1 x)"))
  (it "only disables the specific rule"
    (expect-match
      '[{:rule-name style/plus-one}]
      "#_{:splint/disable [style/plus-zero]} (+ 1 x)"))
  (it "handles quote"
    (expect-match nil
      "'(+ 1 x)")))

(defdescribe throws-in-rules-test
  (it "handles exceptions in rules"
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
      "(very-special-symbol :do-not-match)"))
  (it "doesn't stop the run"
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
      (io/file "corpus" "throw_in_middle.clj"))))

(defdescribe parse-error-test
  (it "gracefully handles parsing errors"
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
      (io/file "corpus" "parse_error.clj"))))

(defdescribe prepare-rules-test
  (it "combines config and rules"
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
          (sut/prepare-rules config rules))))))

(defdescribe update-rules-test
  (given [config (into {} (select-keys @dev/dev-config
                          ['lint/if-else-nil
                           'naming/lisp-case
                           'lint/warn-on-reflection]))
        config (update-vals config #(assoc % :enabled true))
        rules (into {} (select-keys (:rules @global-rules)
                         ['lint/if-else-nil
                          'naming/lisp-case
                          'lint/warn-on-reflection]))
        rules (:rules (sut/prepare-rules config rules))]
    (it "changes nothing"
      (expect (= rules (sut/update-rules rules nil))))
    (it "blanket disable"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled false}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled false}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^:splint/disable []))))
    (it "genre disable"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled false}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled true}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^{:splint/disable ['lint]} []))))
    (it "two disabled genres"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled false}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled false}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^{:splint/disable ['lint 'naming]} []))))
    (it "specific rule"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled true}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled true}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^{:splint/disable ['lint/warn-on-reflection]} []))))
    (it "two specific rules"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled true}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled false}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^{:splint/disable ['naming/lisp-case
                                                     'lint/warn-on-reflection]} []))))
    (it "both genre and specific rule"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled true}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled false}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled false}}}
          (sut/update-rules rules ^{:splint/disable ['naming
                                                     'lint/warn-on-reflection]} []))))
    (it "unmatched genre"
      (expect
        (match?
          {'lint/if-else-nil {:full-name 'lint/if-else-nil
                              :config {:enabled true}}
           'naming/lisp-case {:full-name 'naming/lisp-case
                              :config {:enabled true}}
           'lint/warn-on-reflection {:full-name 'lint/warn-on-reflection
                                     :config {:enabled true}}}
          (sut/update-rules rules ^{:splint/disable ['asdf]} []))))))

(defdescribe require-files-test
  (it "reads in the given files"
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
        (reset! global-rules existing-rules)))))

(defdescribe auto-gen-config-test
  (it "includes all failing diagnostics"
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
          (sut/auto-gen-config [(io/file "corpus" "printer_test.clj")] {:clojure-version {:major 1 :minor 11}}))))))

(defdescribe only-flag-test
  (given [only-test-file (io/file "corpus" "only_test.clj")]
    (it "can select a single rule"
      (expect
        (match?
         {:result {:diagnostics [{:rule-name 'style/plus-one
                                  :filename only-test-file}]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel" "--only" "style/plus-one" "--" (str only-test-file)])))))
    (it "can select a genre"
      (expect
        (match?
         {:result {:diagnostics [{:rule-name 'style/useless-do
                                  :filename only-test-file}
                                 {:rule-name 'style/plus-one
                                  :filename only-test-file}]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel" "--only" "style" "--" (str only-test-file)])))))
    (it "can select multiple rules"
      (expect
        (match?
         {:result {:diagnostics [{:rule-name 'naming/single-segment-namespace
                                  :filename only-test-file}
                                 {:rule-name 'style/plus-one
                                  :filename only-test-file}]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel"
                     "--only" "style/plus-one"
                     "--only" "naming/single-segment-namespace"
                     "--" (str only-test-file)])))))
    (it "can select multiple genres"
      (expect
        (match?
         {:result {:diagnostics [{:rule-name 'naming/single-segment-namespace
                                  :filename only-test-file}
                                 {:rule-name 'style/useless-do
                                  :filename only-test-file}
                                 {:rule-name 'style/plus-one
                                  :filename only-test-file}]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel"
                     "--only" "style"
                     "--only" "naming"
                     "--" (str only-test-file)])))))
    (it "can select mix and match"
      (expect
        (match?
         {:result {:diagnostics [{:rule-name 'naming/single-segment-namespace
                                  :filename only-test-file}
                                 {:rule-name 'style/plus-one
                                  :filename only-test-file}]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel"
                     "--only" "style/plus-one"
                     "--only" "naming"
                     "--" (str only-test-file)])))))
    (it "throws an error if given an incorrect rule or genre"
      (expect
        (match?
         {:result {:exit 1
                   :message string?
                   :errors ["Failed to validate \"--only stool\": Not a valid rule."
                            "Failed to validate \"--only naming/DOES-NOT-MATCH\": Not a valid rule."]}}
         (with-out-str-data-map
           (sut/run ["--no-parallel"
                     "--only" "stool"
                     "--only" "naming/DOES-NOT-MATCH"
                     "--" (str only-test-file)])))))))
