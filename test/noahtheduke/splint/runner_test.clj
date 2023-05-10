(ns noahtheduke.splint.runner-test
  (:require
    [expectations.clojure.test :refer [defexpect]]
    [noahtheduke.splint.test-helpers :refer [expect-match]]))

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
