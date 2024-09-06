; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.runners.autocorrect-test
  (:require
   [lazytest.core :refer [defdescribe expect it]]
   [noahtheduke.splint.test-helpers :refer [check-all prep-dev-config
                                            print-to-file!
                                            with-out-str-data-map
                                            with-temp-files]]
   [lazytest.extensions.matcher-combinators :refer [match?]]
   [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defdescribe autocorrect-test
  (it "checks the same form multiple times"
    (with-temp-files [file "some_file.clj"]
      (print-to-file!
       file
       "(ns some_file)"
       "(do (+ 1 foo))"
       "(= \"hello\" bar)")
      (let [{diagnostics :result
             s :string} (with-out-str-data-map
                          (check-all file (prep-dev-config {:autocorrect true})))]
        (expect
          (match?
           [{:rule-name 'lint/underscore-in-namespace}
            {:rule-name 'naming/single-segment-namespace}
            {:rule-name 'style/useless-do}
            {:rule-name 'style/plus-one}]
           diagnostics))
        (expect
          (str/includes? s "some_file.clj:2")))
      (expect
        (= (str/join "\n"
                     ["(ns some_file)"
                      "(inc foo)"
                      "(= \"hello\" bar)"])
           (str/trim (slurp file))))))
  (it "outputs reader macros correctly"
    (with-temp-files [file "some_file.clj"]
      (print-to-file!
       file
       "(let [foo (atom {:a :b})]"
       "  (assoc a :x :y :z @foo))")
      (let [{diagnostics :result}
            (with-out-str-data-map
              (check-all file (prep-dev-config
                               {:autocorrect true
                                'performance/assoc-many {:enabled true}})))]
        (expect
          (match?
           [{:rule-name 'performance/assoc-many}]
           diagnostics)))
      (expect
        (= (str/join "\n"
                     ["(let [foo (atom {:a :b})]"
                      "  (-> a (assoc :x :y) (assoc :z @foo)))"])
           (str/trim (slurp file))))))
  (it "outputs indentation correctly"
    {:focus true}
    (with-temp-files [file "some_file.clj"]
      (print-to-file!
       file
       "(let [foo (atom {:a :b})]"
       "  (assoc a :xxxxxxxxxxxxxxxxxxxxxxx :yyyyyyyyyyyyyyyyyyyyyyy :z @foo))")
      (let [{diagnostics :result
             _s :string}
            (with-out-str-data-map
              (check-all file (prep-dev-config
                               {:autocorrect true
                                'performance/assoc-many {:enabled true}})))]
        (expect
          (match?
           [{:rule-name 'performance/assoc-many}]
           diagnostics)))
      (expect
        (= (str/join "\n"
                     ["(let [foo (atom {:a :b})]"
                      "  (-> a"
                      "      (assoc :xxxxxxxxxxxxxxxxxxxxxxx :yyyyyyyyyyyyyyyyyyyyyyy)"
                      "      (assoc :z @foo)))"])
           (str/trim (slurp file)))))))
