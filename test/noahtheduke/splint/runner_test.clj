(ns noahtheduke.splint.runner-test 
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-all]]))

(defexpect ignore-rules-test
  (expect nil?
    (check-all "#_:splint/disable (+ 1 x)")))

(defexpect ignore-genre-test
  (expect nil?
    (check-all "#_{:splint/disable [style]} (+ 1 x)")))

(defexpect ignore-specific-rule-test
  (expect nil?
    (check-all "#_{:splint/disable [style/plus-one]} (+ 1 x)")))

(defexpect ignore-unnecessary-rule-test
  (expect some?
    (check-all "#_{:splint/disable [style/plus-zero]} (+ 1 x)")))
