; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.performance-test
  (:require
    [clojure.java.io :as io]
    [clojure.tools.gitlibs :as gl]
    [noahtheduke.splint.config :refer [default-config]]
    [noahtheduke.splint.rules :refer [global-rules]]
    [noahtheduke.splint.runner :refer [run]]))

(set! *warn-on-reflection* true)

(def all-enabled-config
  (update-vals @default-config #(assoc % :enabled true)))

(defn clj-kondo-analyzer-perf-test []
  (let [clj-kondo (gl/procure "https://github.com/clj-kondo/clj-kondo.git" 'clj-kondo/clj-kondo "v2023.05.26")
        analyzer (io/file clj-kondo "src" "clj_kondo" "impl" "analyzer.clj")
        original-rules @global-rules]
    ; v1.3 -> v1.8
    ; (swap! global-rules update-vals #(assoc % :pattern (constantly nil)))
    ; v1.9+
    (swap! global-rules update :rules update-vals #(assoc % :pattern (constantly nil)))
    ; (prn (count (clojure.string/split-lines (slurp analyzer))))
    (with-redefs [noahtheduke.splint.config/load-config all-enabled-config]
      #_:clj-kondo/ignore
      (user/quick-bench
        (with-out-str (run ["--quiet" "--no-parallel" (str analyzer)]))))
    (reset! global-rules original-rules)
    nil))

(comment
  (do
    (clj-kondo-analyzer-perf-test)
    (flush)
    (clj-kondo-analyzer-perf-test)
    (flush))
  )

; clj-kondo.impl.analyzer: 3146 lines

; v1.3.2
; Evaluation count : 18 in 6 samples of 3 calls.
;              Execution time mean : 56.439990 ms
;     Execution time std-deviation : 2.201423 ms
;    Execution time lower quantile : 53.881899 ms ( 2.5%)
;    Execution time upper quantile : 59.188022 ms (97.5%)
;                    Overhead used : 6.961717 ns

; v1.4.1
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 74.981736 ms
;     Execution time std-deviation : 2.692943 ms
;    Execution time lower quantile : 71.375745 ms ( 2.5%)
;    Execution time upper quantile : 78.272635 ms (97.5%)
;                    Overhead used : 8.120804 ns

; v1.5.0
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 64.896980 ms
;     Execution time std-deviation : 4.343168 ms
;    Execution time lower quantile : 60.927301 ms ( 2.5%)
;    Execution time upper quantile : 71.621254 ms (97.5%)
;                    Overhead used : 9.075271 ns

; v1.6.1
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 99.982636 ms
;     Execution time std-deviation : 4.864236 ms
;    Execution time lower quantile : 94.882560 ms ( 2.5%)
;    Execution time upper quantile : 105.508689 ms (97.5%)
;                    Overhead used : 8.054923 ns

; v1.7.0
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 62.227302 ms
;     Execution time std-deviation : 1.925861 ms
;    Execution time lower quantile : 60.104244 ms ( 2.5%)
;    Execution time upper quantile : 65.063141 ms (97.5%)
;                    Overhead used : 8.218798 ns

; v1.8.0
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 70.010786 ms
;     Execution time std-deviation : 2.935414 ms
;    Execution time lower quantile : 67.032078 ms ( 2.5%)
;    Execution time upper quantile : 73.095681 ms (97.5%)
;                    Overhead used : 7.581973 ns

; v1.9.0
; Evaluation count : 12 in 6 samples of 2 calls.
;              Execution time mean : 60.495566 ms
;     Execution time std-deviation : 1.692023 ms
;    Execution time lower quantile : 58.258922 ms ( 2.5%)
;    Execution time upper quantile : 62.431311 ms (97.5%)
;                    Overhead used : 7.220732 ns
