(ns noahtheduke.splint.printer 
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :as pp]))

(defn print-find-dispatch [output _diagnostic] output)

(defmulti print-find #'print-find-dispatch)

(defmethod print-find "full" [_ {:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (pp/pprint form)
  (when alt
    (println "Consider using:")
    (pp/pprint alt))
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
  (pp/pprint form)
  (println "```")
  (newline)
  (when alt
    (println "Consider using:")
    (newline)
    (println "```clojure")
    (pp/pprint alt)
    (println "```")
    (newline))
  (flush))

(defmethod print-find "json" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str))]
    (json/write diagnostic *out* {:escape-slash false})
    (newline)
    (flush)))

(defmethod print-find "json-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str))]
    (json/pprint diagnostic {:escape-slash false})
    (newline)
    (flush)))

(defn print-results
  [options diagnostics total-time]
  (when-not (or (:quiet options) (:silent options))
    (let [printer (get-method print-find (:output options))]
      (doseq [diagnostic (sort-by :filename diagnostics)]
        (printer nil diagnostic))))
  (when-not (or (:silent options) (#{"markdown" "json" "json-pretty"} (:output options)))
    (printf "Linting took %sms, %s style warnings%n"
            total-time
            (count diagnostics))
    (flush)))
