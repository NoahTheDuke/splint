; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.rules.style.prefer-clj-string-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

(defexpect prefer-clj-math-test
  (expect-match
    '[{:alt (clojure.string/reverse "hello world")}]
    "(str (.reverse (StringBuilder. \"hello world\")))")
  (expect-match
    '[{:alt (clojure.string/capitalize s)}
      {:rule-name style/prefer-clj-string
       :form (.toUpperCase (subs s 0 1))
       :message
       "Use the `clojure.string` function instead of interop."
       :alt (clojure.string/upper-case (subs s 0 1))
       :line 1
       :column 6
       :end-row 1
       :end-col 33
       :filename "example.clj"}
      {:rule-name style/prefer-clj-string
       :form (.toLowerCase (subs s 1))
       :message
       "Use the `clojure.string` function instead of interop."
       :alt (clojure.string/lower-case (subs s 1))
       :line 1
       :column 34
       :end-row 1
       :end-col 59
       :filename "example.clj"}]
    "(str (.toUpperCase (subs s 0 1)) (.toLowerCase (subs s 1)))")
  (expect-match
    '[{:alt (clojure.string/upper-case "hello world")}]
    "(.toUpperCase \"hello world\")")
  #_(expect-match
    '[{:alt (str x)}]
    "(.toString x)"))
