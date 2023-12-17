; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runner-test
  (:require
    [clojure.java.io :as io]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.dev :as dev]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :as sut]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

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
      :message "Splint encountered an error during 'dev/throws-on-match: matched"}]
    "(very-special-symbol :do-not-match)")
  (expect-match
    [{:rule-name 'naming/single-segment-namespace
      :form '(ns throw-in-middle)
      :message "throw-in-middle is a single segment. Consider adding an additional segment."
      :alt nil
      :line 5
      :column 1
      :end-row 5
      :end-col 21
      :filename (io/file "corpus/throw_in_middle.clj")}
     {:rule-name 'splint/error
      :form '(very-special-symbol :do-not-match)
      :message "Splint encountered an error during 'dev/throws-on-match: matched"
      :alt nil
      :line 7
      :column 1
      :end-row 7
      :end-col 36
      :filename (io/file "corpus" "throw_in_middle.clj")}
     {:rule-name 'lint/let-if
      :form '(let [a 1] (if a (+ a a) 2))
      :message "Use `if-let` instead of recreating it."
      :alt '(if-let [a 1] (+ a a) 2)
      :line 9
      :column 1
      :end-col 29
      :end-row 9
      :filename (io/file "corpus/throw_in_middle.clj")}]
    (io/file "corpus" "throw_in_middle.clj")))

(defexpect parse-error-test
  (expect-match
    [{:rule-name 'splint/parsing-error
       :form nil
       :message "Splint encountered an error: Map literal contains duplicate key: :a"
       :alt nil
       :line 5
       :column 1
       :end-row nil
       :end-col nil
       :filename (io/file "corpus/parse_error.clj")}]
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
        {:list [{:full-name 'lint/if-else-nil}
                {:full-name 'naming/lisp-case}]
         :file [{:full-name 'lint/warn-on-reflection}]}
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
        rules (sut/prepare-rules config rules)]
    (expect rules (sut/update-rules rules nil))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled false}}
                {:full-name 'naming/lisp-case
                 :config {:enabled false}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^:splint/disable [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled false}}
                {:full-name 'naming/lisp-case
                 :config {:enabled true}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^{:splint/disable ['lint]} [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled false}}
                {:full-name 'naming/lisp-case
                 :config {:enabled false}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^{:splint/disable ['lint 'naming]} [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled true}}
                {:full-name 'naming/lisp-case
                 :config {:enabled true}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^{:splint/disable ['lint/warn-on-reflection]} [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled true}}
                {:full-name 'naming/lisp-case
                 :config {:enabled false}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^{:splint/disable ['naming/lisp-case
                                                   'lint/warn-on-reflection]} [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled true}}
                {:full-name 'naming/lisp-case
                 :config {:enabled false}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled false}}]}
        (sut/update-rules rules ^{:splint/disable ['naming
                                                   'lint/warn-on-reflection]} [])))
    (expect
      (match?
        {:list [{:full-name 'lint/if-else-nil
                 :config {:enabled true}}
                {:full-name 'naming/lisp-case
                 :config {:enabled true}}]
         :file [{:full-name 'lint/warn-on-reflection
                 :config {:enabled true}}]}
        (sut/update-rules rules ^{:splint/disable ['asdf]} [])))))
