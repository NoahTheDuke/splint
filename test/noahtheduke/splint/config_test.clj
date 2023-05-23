(ns noahtheduke.splint.config-test
  (:require
    [expectations.clojure.test :refer [defexpect expect]]
    [noahtheduke.splint.config :as sut]
    [matcher-combinators.test :refer [match?]]))

(defexpect load-config-test
  (expect (match? {:parallel true :output "full"}
                  (sut/load-config nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "clj-kondo"}
                                   {:output "simple"})))
  (expect (match? {:parallel false}
                  (sut/load-config {'parallel false} nil)))
  (expect (match? {:output "simple"}
                  (sut/load-config {'output "simple"} nil))))