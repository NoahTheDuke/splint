; Adapted from clojure.core and cognitect.test-runner
; Clojure: Copyright © Rich Hickey, ELP 1.0
; Cognitect test-runner: Copyright © 2018-2022 Cognitect, ELP 1.0
; Modifications licensed under ELP 1.0

(ns noahtheduke.splint.utils.test-runner
  (:require
    [clojure.test :as t]
    [clojure.tools.cli :as cli]
    [cognitect.test-runner :as ctr]))

(defmacro with-out-str-data-map
  "Adapted from clojure.core/with-out-str.

  Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter. Returns the result of the body and the string created by any
  nested printing calls in a map under the respective keys :result and :string."
  [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [*out* s#]
       (let [r# (do ~@body)]
         {:result r#
          :string (str s#)}))))

(defmacro time-data-map
  "Adapted from clojure.core/time.

  Evaluates expr and records the time it took. Returns the result of the expr,
  the total milliseconds the expr took, and a pretty-printed string of the
  elapsed time in a map under the respective keys :result, :elapsed, and :string."
  [expr]
  `(let [start# (System/currentTimeMillis)
         ret# ~expr
         time# (int (- (System/currentTimeMillis) start#))]
     {:result ret#
      :elapsed time#
      :string (str "Elapsed time: " time# " msecs")}))

;; Don't print namespaces or summary
(defmethod t/report :begin-test-ns [_])
(defmethod t/report :summary [_])

(defn ctr-main
  "Adapted from cognitect.test-runner"
  [& args]
  (let [args (cli/parse-opts args ctr/cli-options)]
    (if (:errors args)
      (do (doseq [e (:errors args)]
            (println e))
          (#'ctr/help args)
          nil)
      (if (-> args :options :test-help)
        (do (#'ctr/help args) nil)
        (time-data-map (:result (with-out-str-data-map (ctr/test (:options args)))))))))

(defn- print-summary
  "Adapted from clojure.test/report :summary"
  [{:keys [result elapsed]}]
  (printf
    "Ran %s tests containing %s assertions in %s msecs.\n%s failures, %s errors.\n"
    (:test result)
    (+ (:pass result) (:fail result) (:error result))
    elapsed
    (:fail result)
    (:error result))
  (flush))

(defn -main [& args]
  (try
    (if-let [results (apply ctr-main args)]
      (do (print-summary results)
          (System/exit (if (zero? (+ (-> results :result :fail)
                                     (-> results :result :error))) 0 1)))
      (System/exit 1))
    (finally (shutdown-agents))))
