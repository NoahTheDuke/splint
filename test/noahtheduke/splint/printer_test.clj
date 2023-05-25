(ns noahtheduke.splint.printer-test
  (:require
    [clojure.string :as str]
    [expectations.clojure.test :refer [defexpect expect]]
    [matcher-combinators.test :refer [match?]]
    [noahtheduke.splint.diagnostic :refer [map->Diagnostic]]
    [noahtheduke.splint.printer :as sut]))

(def diagnostics
  [(map->Diagnostic
     '{:rule-name style/when-do
       :form (when (not (= 1 1)) (do (prn 2) (prn 3)))
       :message "Unnecessary `do` in `when` body."
       :alt (when (not (= 1 1)) (prn 2) (prn 3))
       :line 1
       :column 1
       :end-row 3
       :end-col 16
       :filename "example.clj"})
   (map->Diagnostic
     '{:rule-name style/when-not-call
       :form (when (not (= 1 1)) (do (prn 2) (prn 3)))
       :message "Use `when-not` instead of recreating it."
       :alt (when-not (= 1 1) (do (prn 2) (prn 3)))
       :line 1
       :column 1
       :end-row 3
       :end-col 16
       :filename "example.clj"})
   (map->Diagnostic
     '{:rule-name style/not-eq
       :form (not (= 1 1))
       :message "Use `not=` instead of recreating it."
       :alt (not= 1 1)
       :line 1
       :column 7
       :end-row 1
       :end-col 20
       :filename "example.clj"})])

(defn print-result-lines [output]
  (-> (sut/print-results {:output output} diagnostics 0)
      (with-out-str)
      (str/split-lines)))

(defexpect printer-output-simple-test
  (expect
    (match?
      ["example.clj:1:1 [style/when-do] - Unnecessary `do` in `when` body."
       "example.clj:1:1 [style/when-not-call] - Use `when-not` instead of recreating it."
       "example.clj:1:7 [style/not-eq] - Use `not=` instead of recreating it."
       "Linting took 0ms, 3 style warnings"]
      (print-result-lines "simple"))))

(defexpect printer-output-full-test
  (expect
    (match?
      ["example.clj:1:1 [style/when-do] - Unnecessary `do` in `when` body."
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "Consider using:"
       "(when (not (= 1 1)) (prn 2) (prn 3))"
       ""
       "example.clj:1:1 [style/when-not-call] - Use `when-not` instead of recreating it."
       "(when (not (= 1 1)) (do (prn 2) (prn 3)))"
       "Consider using:"
       "(when-not (= 1 1) (do (prn 2) (prn 3)))"
       ""
       "example.clj:1:7 [style/not-eq] - Use `not=` instead of recreating it."
       "(not (= 1 1))"
       "Consider using:"
       "(not= 1 1)"
       ""
       "Linting took 0ms, 3 style warnings"]
      (print-result-lines "full"))))

(defexpect printer-output-clj-kondo-test
  (expect
    (match?
      ["example.clj:1:1: warning: Unnecessary `do` in `when` body."
       "example.clj:1:1: warning: Use `when-not` instead of recreating it."
       "example.clj:1:7: warning: Use `not=` instead of recreating it."
       "Linting took 0ms, 3 style warnings"]
      (print-result-lines "clj-kondo"))))

(defexpect printer-output-markdown-test
  (expect
    (match?
      ["----"
       ""
       "#### example.clj:1:1 [style/when-do]"
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
       "#### example.clj:1:1 [style/when-not-call]"
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
       "#### example.clj:1:7 [style/not-eq]"
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
      ["{\"rule-name\":\"style/when-do\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"message\":\"Unnecessary `do` in `when` body.\",\"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\",\"line\":1,\"column\":1,\"end-row\":3,\"end-col\":16,\"filename\":\"example.clj\"}"
       "{\"rule-name\":\"style/when-not-call\",\"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\",\"message\":\"Use `when-not` instead of recreating it.\",\"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\",\"line\":1,\"column\":1,\"end-row\":3,\"end-col\":16,\"filename\":\"example.clj\"}"
       "{\"rule-name\":\"style/not-eq\",\"form\":\"(not (= 1 1))\",\"message\":\"Use `not=` instead of recreating it.\",\"alt\":\"(not= 1 1)\",\"line\":1,\"column\":7,\"end-row\":1,\"end-col\":20,\"filename\":\"example.clj\"}"]
      (print-result-lines "json"))))

(defexpect printer-output-json-pretty-test
  (expect
    (match?
      ["{\"rule-name\":\"style/when-do\","
       " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
       " \"message\":\"Unnecessary `do` in `when` body.\","
       " \"alt\":\"(when (not (= 1 1)) (prn 2) (prn 3))\","
       " \"line\":1,"
       " \"column\":1,"
       " \"end-row\":3,"
       " \"end-col\":16,"
       " \"filename\":\"example.clj\"}"
       ""
       "{\"rule-name\":\"style/when-not-call\","
       " \"form\":\"(when (not (= 1 1)) (do (prn 2) (prn 3)))\","
       " \"message\":\"Use `when-not` instead of recreating it.\","
       " \"alt\":\"(when-not (= 1 1) (do (prn 2) (prn 3)))\","
       " \"line\":1,"
       " \"column\":1,"
       " \"end-row\":3,"
       " \"end-col\":16,"
       " \"filename\":\"example.clj\"}"
       ""
       "{\"rule-name\":\"style/not-eq\","
       " \"form\":\"(not (= 1 1))\","
       " \"message\":\"Use `not=` instead of recreating it.\","
       " \"alt\":\"(not= 1 1)\","
       " \"line\":1,"
       " \"column\":7,"
       " \"end-row\":1,"
       " \"end-col\":20,"
       " \"filename\":\"example.clj\"}"]
      (print-result-lines "json-pretty"))))
