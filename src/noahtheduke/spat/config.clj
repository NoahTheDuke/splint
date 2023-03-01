(ns noahtheduke.spat.config 
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(def default-config
  (delay (edn/read-string (slurp (io/resource "config/default.edn")))))

(defn find-local-config []
  (loop [dir (.getParentFile (.getAbsoluteFile (io/file ".")))]
    (let [config (io/file dir ".splint.edn")]
      (if (.exists config)
        (edn/read-string (slurp config))
        (when-let [parent (.getParentFile dir)]
          (recur parent))))))

(defn deep-merge [& maps]
  (letfn [(reconcile-keys [val-in-result val-in-latter]
            (if (and (map? val-in-result)
                     (map? val-in-latter))
              (merge-with reconcile-keys val-in-result val-in-latter)
              val-in-latter))
          (reconcile-maps [result latter]
            (merge-with reconcile-keys result latter))]
    (reduce reconcile-maps {} maps)))

(defn load-config []
  (let [local (find-local-config)]
    (deep-merge @default-config local)))
