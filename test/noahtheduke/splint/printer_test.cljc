; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.printer-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [lazytest.core :refer [defdescribe describe expect it]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [noahtheduke.splint.parser :as parser]
   [noahtheduke.splint.printer :as sut :refer [*fipp-width*]]
   [noahtheduke.splint.test-helpers :refer [check-all]]))

(set! *warn-on-reflection* true)

(def diagnostics
  (->> (check-all (io/file "corpus" "printer_test.clj")
         {'naming/single-segment-namespace {:enabled false}})
    (map #(update % :exception
            (fn [ex]
              (cond-> ex
                (:trace ex)
                (assoc :trace [['noahtheduke.splint.class1 'method1 "file1" 1]
                               ['noahtheduke.splint.class2 'method2 "file2" 2]
                               ['noahtheduke.splint.class3 'method3 "file3" 3]])
                (-> ex :via not-empty)
                (assoc :via
                  [(assoc (first (:via ex))
                     :at ['noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__
                          'invoke "throws_on_match.clj" 16])])))))))

(defn print-result-lines [output]
  (->> diagnostics
    (sort-by sut/sort-fn)
    (mapcat #(str/split-lines (with-out-str (sut/print-find output %))))
    (vec)))

(defdescribe print-results-test
  (describe ":output"
    (it "simple"
      (expect
        (match?
          ["corpus/printer_test.clj:7:1 [style/when-not-call] - Use `when-not` instead of recreating it."
           "corpus/printer_test.clj:7:1 [style/when-do] - Unnecessary `do` in `when` body."
           "corpus/printer_test.clj:8:3 [style/not-eq] - Use `not=` instead of recreating it."
           "corpus/printer_test.clj:14:1 [splint/error] - Splint encountered an error during 'dev/throws-on-match: matched"
           "Linting took 0ms, checked 1 files, 3 style warnings, 1 errors"]
          (-> (sut/print-results {:config {:output "simple"}
                                  :diagnostics diagnostics
                                  :total-time 0
                                  :checked-files (distinct (mapv (comp str :filename) diagnostics))})
              (with-out-str)
              (str/split-lines)))))
    (it "full"
      (expect
        (match?
          ["corpus/printer_test.clj:7:1 [style/when-not-call] - Use `when-not` instead of recreating it."
           "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
           "Consider using:"
           "(when-not (= 1 1) (do (prn 2) (prn 3)))"
           "corpus/printer_test.clj:7:1 [style/when-do] - Unnecessary `do` in `when` body."
           "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
           "Consider using:"
           "(when (not (= 1 1)) (prn 2) (prn 3))"
           "corpus/printer_test.clj:8:3 [style/not-eq] - Use `not=` instead of recreating it."
           "(not (= 1 1))"
           "Consider using:"
           "(not= 1 1)"
           "corpus/printer_test.clj:14:1 [splint/error] - Splint encountered an error during 'dev/throws-on-match: matched"
           "(very-special-symbol :do-not-match)"]
          (print-result-lines "full"))))
    (it "clj-kondo"
      (expect
        (match?
          ["corpus/printer_test.clj:7:1: warning: Use `when-not` instead of recreating it."
           "corpus/printer_test.clj:7:1: warning: Unnecessary `do` in `when` body."
           "corpus/printer_test.clj:8:3: warning: Use `not=` instead of recreating it."
           "corpus/printer_test.clj:14:1: warning: Splint encountered an error during 'dev/throws-on-match: matched"]
          (print-result-lines "clj-kondo"))))
    (it "markdown"
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
           "```"
           "----"
           ""
           "#### corpus/printer_test.clj:14:1 [splint/error]"
           ""
           "Splint encountered an error during 'dev/throws-on-match: matched"
           ""
           "```clojure"
           "(very-special-symbol :do-not-match)"
           "```"]
          (print-result-lines "markdown"))))
    (it "json"
      (expect
        (match?
          ["{\"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\",\"column\":1,\"end-column\":14,\"end-line\":12,\"exception\":null,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"line\":7,\"message\":\"Use `when-not` instead of recreating it.\",\"rule-name\":\"style/when-not-call\"}"
           "{\"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\",\"column\":1,\"end-column\":14,\"end-line\":12,\"exception\":null,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"line\":7,\"message\":\"Unnecessary `do` in `when` body.\",\"rule-name\":\"style/when-do\"}"
           "{\"alt\":\"(not= 1 1)\",\"column\":3,\"end-column\":13,\"end-line\":9,\"exception\":null,\"filename\":\"corpus/printer_test.clj\",\"form\":\"(not (= 1 1))\",\"line\":8,\"message\":\"Use `not=` instead of recreating it.\",\"rule-name\":\"style/not-eq\"}"
           "{\"alt\":\"nil\",\"column\":1,\"end-column\":36,\"end-line\":14,\"exception\":{\"via\":[{\"type\":\"clojure.lang.ExceptionInfo\",\"message\":\"matched\",\"data\":{\"extra\":\"data\"},\"at\":[\"noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__\",\"invoke\",\"throws_on_match.clj\",16]}],\"trace\":[\"noahtheduke.splint.class1.method1 (file1:1)\",\"noahtheduke.splint.class2.method2 (file2:2)\",\"noahtheduke.splint.class3.method3 (file3:3)\"],\"cause\":\"matched\",\"data\":{\"extra\":\"data\"}},\"filename\":\"corpus/printer_test.clj\",\"form\":\"(very-special-symbol :do-not-match)\",\"line\":14,\"message\":\"Splint encountered an error during 'dev/throws-on-match: matched\",\"rule-name\":\"splint/error\"}"]
          (print-result-lines "json"))))
    (it "json-pretty"
      (expect
        (match?
          #?(:bb ["{"
                  "  \"alt\" : \"(when-not (= 1 1) (do (prn 2) (prn 3)))\","
                  "  \"column\" : 1,"
                  "  \"end-column\" : 14,"
                  "  \"end-line\" : 12,"
                  "  \"exception\" : null,"
                  "  \"filename\" : \"corpus/printer_test.clj\","
                  "  \"form\" : \"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
                  "  \"line\" : 7,"
                  "  \"message\" : \"Use `when-not` instead of recreating it.\","
                  "  \"rule-name\" : \"style/when-not-call\""
                  "}"
                  "{"
                  "  \"alt\" : \"(when (not (= 1 1)) (prn 2) (prn 3))\","
                  "  \"column\" : 1,"
                  "  \"end-column\" : 14,"
                  "  \"end-line\" : 12,"
                  "  \"exception\" : null,"
                  "  \"filename\" : \"corpus/printer_test.clj\","
                  "  \"form\" : \"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
                  "  \"line\" : 7,"
                  "  \"message\" : \"Unnecessary `do` in `when` body.\","
                  "  \"rule-name\" : \"style/when-do\""
                  "}"
                  "{"
                  "  \"alt\" : \"(not= 1 1)\","
                  "  \"column\" : 3,"
                  "  \"end-column\" : 13,"
                  "  \"end-line\" : 9,"
                  "  \"exception\" : null,"
                  "  \"filename\" : \"corpus/printer_test.clj\","
                  "  \"form\" : \"(not (= 1 1))\","
                  "  \"line\" : 8,"
                  "  \"message\" : \"Use `not=` instead of recreating it.\","
                  "  \"rule-name\" : \"style/not-eq\""
                  "}"
                  "{"
                  "  \"alt\" : \"nil\","
                  "  \"column\" : 1,"
                  "  \"end-column\" : 36,"
                  "  \"end-line\" : 14,"
                  "  \"exception\" : {"
                  "    \"via\" : [ {"
                  "      \"type\" : \"clojure.lang.ExceptionInfo\","
                  "      \"message\" : \"matched\","
                  "      \"data\" : {"
                  "        \"extra\" : \"data\""
                  "      },"
                  "      \"at\" : [ \"noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__\", \"invoke\", \"throws_on_match.clj\", 16 ]"
                  "    } ],"
                  "    \"trace\" : [ \"noahtheduke.splint.class1.method1 (file1:1)\", \"noahtheduke.splint.class2.method2 (file2:2)\", \"noahtheduke.splint.class3.method3 (file3:3)\" ],"
                  "    \"cause\" : \"matched\","
                  "    \"data\" : {"
                  "      \"extra\" : \"data\""
                  "    }"
                  "  },"
                  "  \"filename\" : \"corpus/printer_test.clj\","
                  "  \"form\" : \"(very-special-symbol :do-not-match)\","
                  "  \"line\" : 14,"
                  "  \"message\" : \"Splint encountered an error during 'dev/throws-on-match: matched\","
                  "  \"rule-name\" : \"splint/error\""
                  "}"]
             :clj ["{\"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\","
                   " \"column\":1,"
                   " \"end-column\":14,"
                   " \"end-line\":12,"
                   " \"exception\":null,"
                   " \"filename\":\"corpus/printer_test.clj\","
                   " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
                   " \"line\":7,"
                   " \"message\":\"Use `when-not` instead of recreating it.\","
                   " \"rule-name\":\"style/when-not-call\"}"
                   "{\"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\","
                   " \"column\":1,"
                   " \"end-column\":14,"
                   " \"end-line\":12,"
                   " \"exception\":null,"
                   " \"filename\":\"corpus/printer_test.clj\","
                   " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
                   " \"line\":7,"
                   " \"message\":\"Unnecessary `do` in `when` body.\","
                   " \"rule-name\":\"style/when-do\"}"
                   "{\"alt\":\"(not= 1 1)\","
                   " \"column\":3,"
                   " \"end-column\":13,"
                   " \"end-line\":9,"
                   " \"exception\":null,"
                   " \"filename\":\"corpus/printer_test.clj\","
                   " \"form\":\"(not (= 1 1))\","
                   " \"line\":8,"
                   " \"message\":\"Use `not=` instead of recreating it.\","
                   " \"rule-name\":\"style/not-eq\"}"
                   "{\"alt\":\"nil\","
                   " \"column\":1,"
                   " \"end-column\":36,"
                   " \"end-line\":14,"
                   " \"exception\":"
                   " {\"via\":"
                   "  [{\"type\":\"clojure.lang.ExceptionInfo\","
                   "    \"message\":\"matched\","
                   "    \"data\":{\"extra\":\"data\"},"
                   "    \"at\":"
                   "    [\"noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__\","
                   "     \"invoke\", \"throws_on_match.clj\", 16]}],"
                   "  \"trace\":"
                   "  [\"noahtheduke.splint.class1.method1 (file1:1)\","
                   "   \"noahtheduke.splint.class2.method2 (file2:2)\","
                   "   \"noahtheduke.splint.class3.method3 (file3:3)\"],"
                   "  \"cause\":\"matched\","
                   "  \"data\":{\"extra\":\"data\"}},"
                   " \"filename\":\"corpus/printer_test.clj\","
                   " \"form\":\"(very-special-symbol :do-not-match)\","
                   " \"line\":14,"
                   " \"message\":"
                   " \"Splint encountered an error during 'dev/throws-on-match: matched\","
                   " \"rule-name\":\"splint/error\"}"])
          (print-result-lines "json-pretty"))))
    (it "edn"
      (expect
        (match?
          ["{:alt (when-not (= 1 1) (do (prn 2) (prn 3))), :column 1, :end-column 14, :end-line 12, :exception nil, :filename \"corpus/printer_test.clj\", :form (when (not (= 1 1)) (do (prn 2) (prn 3))), :line 7, :message \"Use `when-not` instead of recreating it.\", :rule-name style/when-not-call}"
           "{:alt (when (not (= 1 1)) (prn 2) (prn 3)), :column 1, :end-column 14, :end-line 12, :exception nil, :filename \"corpus/printer_test.clj\", :form (when (not (= 1 1)) (do (prn 2) (prn 3))), :line 7, :message \"Unnecessary `do` in `when` body.\", :rule-name style/when-do}"
           "{:alt (not= 1 1), :column 3, :end-column 13, :end-line 9, :exception nil, :filename \"corpus/printer_test.clj\", :form (not (= 1 1)), :line 8, :message \"Use `not=` instead of recreating it.\", :rule-name style/not-eq}"
           "{:alt nil, :column 1, :end-column 36, :end-line 14, :exception {:via [{:type clojure.lang.ExceptionInfo, :message \"matched\", :data {:extra :data}, :at [noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__ invoke \"throws_on_match.clj\" 16]}], :trace [\"noahtheduke.splint.class1.method1 (file1:1)\" \"noahtheduke.splint.class2.method2 (file2:2)\" \"noahtheduke.splint.class3.method3 (file3:3)\"], :cause \"matched\", :data {:extra :data}}, :filename \"corpus/printer_test.clj\", :form (very-special-symbol :do-not-match), :line 14, :message \"Splint encountered an error during 'dev/throws-on-match: matched\", :rule-name splint/error}"]
          (print-result-lines "edn"))))
    (it "edn-pretty"
      (expect
        (match?
          ["{:alt (when-not (= 1 1) (do (prn 2) (prn 3))),"
             " :column 1,"
             " :end-column 14,"
             " :end-line 12,"
             " :exception nil,"
             " :filename \"corpus/printer_test.clj\","
             " :form (when (not (= 1 1)) (do (prn 2) (prn 3))),"
             " :line 7,"
             " :message \"Use `when-not` instead of recreating it.\","
             " :rule-name style/when-not-call}"
           "{:alt (when (not (= 1 1)) (prn 2) (prn 3)),"
             " :column 1,"
             " :end-column 14,"
             " :end-line 12,"
             " :exception nil,"
             " :filename \"corpus/printer_test.clj\","
             " :form (when (not (= 1 1)) (do (prn 2) (prn 3))),"
             " :line 7,"
             " :message \"Unnecessary `do` in `when` body.\","
             " :rule-name style/when-do}"
           "{:alt (not= 1 1),"
             " :column 3,"
             " :end-column 13,"
             " :end-line 9,"
             " :exception nil,"
             " :filename \"corpus/printer_test.clj\","
             " :form (not (= 1 1)),"
             " :line 8,"
             " :message \"Use `not=` instead of recreating it.\","
             " :rule-name style/not-eq}"
           "{:alt nil,"
             " :column 1,"
             " :end-column 36,"
             " :end-line 14,"
             " :exception {:via [{:type clojure.lang.ExceptionInfo,"
                                  "                    :message \"matched\","
                                  "                    :data {:extra :data},"
                                  "                    :at [noahtheduke.splint.rules.dev.throws_on_match$eval$or__auto__"
                                                            "                         invoke"
                                                            "                         \"throws_on_match.clj\""
                                                            "                         16]}],"
                           "             :trace [\"noahtheduke.splint.class1.method1 (file1:1)\""
                                                 "                     \"noahtheduke.splint.class2.method2 (file2:2)\""
                                                 "                     \"noahtheduke.splint.class3.method3 (file3:3)\"],"
                           "             :cause \"matched\","
                           "             :data {:extra :data}},"
             " :filename \"corpus/printer_test.clj\","
             " :form (very-special-symbol :do-not-match),"
             " :line 14,"
             " :message \"Splint encountered an error during 'dev/throws-on-match: matched\","
             " :rule-name splint/error}"]
          (print-result-lines "edn-pretty"))))))

(defdescribe print-form-test
  (it "prints all special characters"
    (let [f (slurp (io/file "corpus" "special_characters.clj"))
          parsed (parser/parse-file {:contents f :ext :clj})]
      (expect (= (format "[%s]"
                         (->> ["@a"
                               "@(a)"
                               "#(+ 1 %)"
                               "#=(+ 1 2)"
                               "#\"a\""
                               "#'a"
                               "#'(a b)"
                               "`a"
                               "`(a b)"
                               "~a"
                               "~(a b)"
                               "~@a"
                               "~@(a b)"]
                              (str/join "\n ")))
                 (binding [*fipp-width* 70]
                   (->> parsed
                        (sut/print-form)
                        (with-out-str)
                        (str/trim))))))))
