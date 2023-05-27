; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

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
  (newline))

(defmethod print-find "simple" [_ {:keys [filename rule-name line column message]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline))

(defmethod print-find "clj-kondo" [_ {:keys [filename line column message]}]
  (printf "%s:%s:%s: warning: %s" filename line column message)
  (newline))

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
    (newline)))

(defmethod print-find "json" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str)
                       (->> (into (sorted-map))))]
    (json/write diagnostic *out* {:escape-slash false})
    (newline)))

(defmethod print-find "json-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str)
                       (->> (into (sorted-map))))]
    (json/pprint diagnostic {:escape-slash false})
    (newline)))

(defmethod print-find "edn" [_ diagnostic]
  (prn (into (sorted-map) diagnostic)))

(defmethod print-find "edn-pretty" [_ diagnostic]
  (pp/pprint (into (sorted-map) diagnostic)))

(defn print-results
  [options diagnostics total-time]
  (when-not (or (:quiet options) (:silent options))
    (let [printer (get-method print-find (:output options))]
      (doseq [diagnostic (sort-by :filename diagnostics)]
        (printer nil diagnostic))
      (flush)))
  (when-not (or (:silent options) (#{"markdown" "json" "json-pretty"
                                     "edn" "edn-pretty"} (:output options)))
    (printf "Linting took %sms, %s style warnings%s\n"
            total-time
            (count (remove #(= 'splint/error (:rule-name %)) diagnostics))
            (if-let [errors (seq (filter #(= 'splint/error (:rule-name %)) diagnostics))]
              (format ", %s errors" (count errors))
              ""))
    (flush)))
