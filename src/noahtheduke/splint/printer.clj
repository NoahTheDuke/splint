(ns noahtheduke.splint.printer 
  (:require
    [clojure.pprint :as pprint]))

(defn print-find-dispatch [output _diagnostic] output)

(defmulti print-find #'print-find-dispatch)

(defmethod print-find "full" [_ {:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (pprint/pprint form)
  (when alt
    (println "Consider using:")
    (pprint/pprint alt))
  (newline)
  (flush))

(defmethod print-find "simple" [_ {:keys [filename rule-name line column message]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (flush))

(defmethod print-find "clj-kondo" [_ {:keys [filename line column message]}]
  (printf "%s:%s:%s: warning: %s" filename line column message)
  (newline)
  (flush))

(defmethod print-find "markdown" [_ {:keys [filename rule-name form line column message alt]}]
  (println "----")
  (newline)
  (printf "#### %s:%s:%s [%s]" filename line column rule-name)
  (newline)
  (newline)
  (println message)
  (newline)
  (println "```clojure")
  (pprint/pprint form)
  (println "```")
  (newline)
  (when alt
    (println "Consider using:")
    (newline)
    (println "```clojure")
    (pprint/pprint alt)
    (println "```")
    (newline))
  (flush))

(defn print-results
  [options diagnostics total-time]
  (when-not (:quiet options)
    (let [printer (get-method print-find (:output options))]
      (doseq [diagnostic (sort-by :filename diagnostics)]
        (printer nil diagnostic))))
  (printf "Linting took %sms, %s style warnings%n"
          total-time
          (count diagnostics))
  (flush))
