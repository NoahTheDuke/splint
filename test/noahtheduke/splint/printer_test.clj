; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.printer-test
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.printer :as sut]
    [noahtheduke.splint.test-helpers :refer [check-all]]))

(def diagnostics
  (check-all (io/file "corpus" "printer_test.clj")
             '{naming/single-segment-namespace {:enabled false}}))

(defn print-result-lines [output]
  (-> (sut/print-results {:config {:output output}
                          :diagnostics diagnostics
                          :total-time 0
                          :checked-files (mapv (comp str :filename) diagnostics)})
      (with-out-str)
      (str/split-lines)))

(defexpect printer-output-simple-test
  (expect
    (match?
      ["corpus/printer_test.clj:7:1 [style/when-not-call] - Use `when-not` instead of recreating it."
       "corpus/printer_test.clj:7:1 [style/when-do] - Unnecessary `do` in `when` body."
       "corpus/printer_test.clj:8:3 [style/not-eq] - Use `not=` instead of recreating it."
       "Linting took 0ms, checked 3 files, 3 style warnings"]
      (print-result-lines "simple"))))

(defexpect printer-output-full-test
  (expect
    (match?
      ["corpus/printer_test.clj:7:1 [style/when-not-call] - Use `when-not` instead of recreating it."
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "Consider using:"
       "(when-not (= 1 1) (do (prn 2) (prn 3)))"
       ""
       "corpus/printer_test.clj:7:1 [style/when-do] - Unnecessary `do` in `when` body."
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "Consider using:"
       "(when (not (= 1 1)) (prn 2) (prn 3))"
       ""
       "corpus/printer_test.clj:8:3 [style/not-eq] - Use `not=` instead of recreating it."
       "(not (= 1 1))"
       "Consider using:"
       "(not= 1 1)"
       ""
       "Linting took 0ms, checked 3 files, 3 style warnings"]
      (print-result-lines "full"))))

(defexpect printer-output-clj-kondo-test
  (expect
    (match?
      ["corpus/printer_test.clj:7:1: warning: Use `when-not` instead of recreating it."
       "corpus/printer_test.clj:7:1: warning: Unnecessary `do` in `when` body."
       "corpus/printer_test.clj:8:3: warning: Use `not=` instead of recreating it."
       "Linting took 0ms, checked 3 files, 3 style warnings"]
      (print-result-lines "clj-kondo"))))

(defexpect printer-output-markdown-test
  (expect
    (match?
      ["----"
       ""
       "#### corpus/printer_test.clj:7:1 [style/when-not-call]"
       ""
       "Use `when-not` instead of recreating it."
       ""
       "```clojure"
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "```"
       ""
       "Consider using:"
       ""
       "```clojure"
       "(when-not (= 1 1) (do (prn 2) (prn 3)))"
       "```"
       ""
       "----"
       ""
       "#### corpus/printer_test.clj:7:1 [style/when-do]"
       ""
       "Unnecessary `do` in `when` body."
       ""
       "```clojure"
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "```"
       ""
       "Consider using:"
       ""
       "```clojure"
       "(when (not (= 1 1)) (prn 2) (prn 3))"
       "```"
       ""
       "----"
       ""
       "#### corpus/printer_test.clj:8:3 [style/not-eq]"
       ""
       "Use `not=` instead of recreating it."
       ""
       "```clojure"
       "(not (= 1 1))"
       "```"
       ""
       "Consider using:"
       ""
       "```clojure"
       "(not= 1 1)"
       "```"]
      (print-result-lines "markdown"))))

(defexpect printer-output-json-test
  (expect
    (match?
      ["{\"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\",\"column\":1,\"end-col\":14,\"end-row\":12,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"line\":7,\"message\":\"Use `when-not` instead of recreating it.\",\"rule-name\":\"style/when-not-call\"}"
       "{\"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\",\"column\":1,\"end-col\":14,\"end-row\":12,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"line\":7,\"message\":\"Unnecessary `do` in `when` body.\",\"rule-name\":\"style/when-do\"}"
       "{\"alt\":\"(not= 1 1)\",\"column\":3,\"end-col\":13,\"end-row\":9,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(not (= 1 1))\",\"line\":8,\"message\":\"Use `not=` instead of recreating it.\",\"rule-name\":\"style/not-eq\"}"]
      (print-result-lines "json"))))

(defexpect printer-output-json-pretty-test
  (expect
    (match?
      ["{\"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\","
       " \"column\":1,"
       " \"end-col\":14,"
       " \"end-row\":12,"
       " \"filename\":\"corpus/printer_test.clj\","
       " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
       " \"line\":7,"
       " \"message\":\"Use `when-not` instead of recreating it.\","
       " \"rule-name\":\"style/when-not-call\"}"
       ""
       "{\"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\","
       " \"column\":1,"
       " \"end-col\":14,"
       " \"end-row\":12,"
       " \"filename\":\"corpus/printer_test.clj\","
       " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
       " \"line\":7,"
       " \"message\":\"Unnecessary `do` in `when` body.\","
       " \"rule-name\":\"style/when-do\"}"
       ""
       "{\"alt\":\"(not= 1 1)\","
       " \"column\":3,"
       " \"end-col\":13,"
       " \"end-row\":9,"
       " \"filename\":\"corpus/printer_test.clj\","
       " \"form\":\"(not (= 1 1))\","
       " \"line\":8,"
       " \"message\":\"Use `not=` instead of recreating it.\","
       " \"rule-name\":\"style/not-eq\"}"]
      (print-result-lines "json-pretty"))))

(defexpect printer-output-edn-test
  (expect
    (match?
      ["{:alt (when-not (= 1 1) (do (prn 2) (prn 3))), :column 1, :end-col 14, :end-row 12, :filename \"corpus/printer_test.clj\", :form (when (not (= 1 1)) (do (prn 2) (prn 3))), :line 7, :message \"Use `when-not` instead of recreating it.\", :rule-name style/when-not-call}"
       "{:alt (when (not (= 1 1)) (prn 2) (prn 3)), :column 1, :end-col 14, :end-row 12, :filename \"corpus/printer_test.clj\", :form (when (not (= 1 1)) (do (prn 2) (prn 3))), :line 7, :message \"Unnecessary `do` in `when` body.\", :rule-name style/when-do}"
       "{:alt (not= 1 1), :column 3, :end-col 13, :end-row 9, :filename \"corpus/printer_test.clj\", :form (not (= 1 1)), :line 8, :message \"Use `not=` instead of recreating it.\", :rule-name style/not-eq}"]
      (print-result-lines "edn"))))

(defexpect printer-output-edn-pretty-test
  (expect
    (match?
      ["{:alt (when-not (= 1 1) (do (prn 2) (prn 3))),"
       " :column 1,"
       " :end-col 14,"
       " :end-row 12,"
       " :filename \"corpus/printer_test.clj\","
       " :form (when (not (= 1 1)) (do (prn 2) (prn 3))),"
       " :line 7,"
       " :message \"Use `when-not` instead of recreating it.\","
       " :rule-name style/when-not-call}"
       "{:alt (when (not (= 1 1)) (prn 2) (prn 3)),"
       " :column 1,"
       " :end-col 14,"
       " :end-row 12,"
       " :filename \"corpus/printer_test.clj\","
       " :form (when (not (= 1 1)) (do (prn 2) (prn 3))),"
       " :line 7,"
       " :message \"Unnecessary `do` in `when` body.\","
       " :rule-name style/when-do}"
       "{:alt (not= 1 1),"
       " :column 3,"
       " :end-col 13,"
       " :end-row 9,"
       " :filename \"corpus/printer_test.clj\","
       " :form (not (= 1 1)),"
       " :line 8,"
       " :message \"Use `not=` instead of recreating it.\","
       " :rule-name style/not-eq}"]
      (print-result-lines "edn-pretty"))))
