; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.printer
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :as pp]
    [noahtheduke.splint.replace :refer [revert-splint-reader-macros]]))

(defmacro print-form [form]
  `(pp/pprint (revert-splint-reader-macros ~form)))

(defn print-find-dispatch [output _diagnostic] output)

(defmulti print-find #'print-find-dispatch)

(defmethod print-find "full" [_ {:keys [filename rule-name form line column message alt]}]
  (printf "%s:%s:%s [%s] - %s" filename line column rule-name message)
  (newline)
  (when form
    (print-form form))
  (when alt
    (println "Consider using:")
    (print-form alt))
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
  (when form
    (println "```clojure")
    (print-form form)
    (println "```")
    (newline))
  (when alt
    (println "Consider using:")
    (newline)
    (println "```clojure")
    (print-form alt)
    (println "```")
    (newline)))

(defmethod print-find "json" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str)
                       (update :filename str)
                       (->> (into (sorted-map))))]
    (json/write diagnostic *out* {:escape-slash false})
    (newline)))

(defmethod print-find "json-pretty" [_ diagnostic]
  (let [diagnostic (-> diagnostic
                       (update :rule-name pr-str)
                       (update :form pr-str)
                       (update :alt pr-str)
                       (update :filename str)
                       (->> (into (sorted-map))))]
    (json/pprint diagnostic {:escape-slash false})
    (newline)))

(defmethod print-find "edn" [_ diagnostic]
  (let [diagnostic (update diagnostic :filename str)]
   (prn (into (sorted-map) diagnostic))))

(defmethod print-find "edn-pretty" [_ diagnostic]
  (let [diagnostic (update diagnostic :filename str)]
    (pp/pprint (into (sorted-map) diagnostic))))

(defn print-results
  [{:keys [config diagnostics checked-files total-time]}]
  (when-not (or (:quiet config) (:silent config))
    (let [printer (get-method print-find (:output config))]
      (doseq [diagnostic (sort-by :filename diagnostics)]
        (printer nil diagnostic))
      (flush)))
  (when-not (or (:silent config)
                (false? (:summary config))
                (#{"markdown" "json" "json-pretty"
                   "edn" "edn-pretty"} (:output config)))
    (printf "Linting took %sms, checked %s files, %s style warnings%s\n"
            total-time
            (count checked-files)
            (count (remove #(= 'splint/error (:rule-name %)) diagnostics))
            (if-let [errors (seq (filter #(= 'splint/error (:rule-name %)) diagnostics))]
              (format ", %s errors" (count errors))
              ""))
    (flush)))
