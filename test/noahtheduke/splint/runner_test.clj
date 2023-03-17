(ns noahtheduke.splint.runner-test 
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint-test :refer [check-all]]))

(defexpect ignore-rules-test
  (expect nil?
    (check-all "#_:splint/disable (+ 1 x)"))
  (expect nil?
    (check-all "#_{:splint/disable [lint]} (+ 1 x)"))
  (expect nil?
    (check-all "#_{:splint/disable [lint/plus-one]} (+ 1 x)"))
  (expect some?
    (check-all "#_{:splint/disable [lint/plus-zero]} (+ 1 x)")))
