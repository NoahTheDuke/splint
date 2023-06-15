; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.clojure-ext.core
  (:import
    (java.util.concurrent Executors Future)
    (clojure.lang LazilyPersistentVector PersistentList)))

(set! *warn-on-reflection* true)

(defn ->list [coll]
  #?(:clj (PersistentList/create coll)
     :bb (apply list coll)))

(defn mapv*
  [f coll]
  #?(:clj (let [cnt (count coll)]
            (if (zero? cnt) []
              (let [new-coll (object-array cnt)
                    iter (.iterator ^Iterable coll)]
                (loop [n (int 0)]
                  (when (.hasNext iter)
                    (aset new-coll n (f (.next iter)))
                    (recur (unchecked-inc n))))
                (LazilyPersistentVector/createOwning new-coll))))
     :bb (mapv f coll)))

(defn run!*
  [f coll]
  #?(:clj (let [cnt (count coll)]
            (if (zero? cnt) []
              (let [iter (.iterator ^Iterable coll)]
                (while (.hasNext iter)
                  (f (.next iter)))
                nil)))
     :bb (run! f coll)))

(defn pmap* [f coll]
  (let [thread-count (+ 2 (.availableProcessors (Runtime/getRuntime)))
        executor (Executors/newFixedThreadPool thread-count)
        futures (mapv #(.submit executor (reify Callable (call [_] (f %)))) coll)
        ret (mapv #(.get ^Future %) futures)]
    (.shutdownNow executor)
    ret))

(comment
  (require 'user)

  (let [coll (range 10000)]
    (doall coll)
    (user/quick-bench
      (->list coll))
    (flush)
    (user/quick-bench
      (apply list coll)))

  (let [coll (range 1000)]
    (println "doall map")
    (user/quick-bench
      (do (doall (map inc coll))
          (doall (map inc []))))
    (println "into []")
    (user/quick-bench
      (do (into [] (map inc) coll)
          (into [] (map inc) [])))
    (println "mapv")
    (user/quick-bench
      (do (mapv inc coll)
          (mapv inc [])))
    (println "mapv*")
    (user/quick-bench
      (do (mapv* inc coll)
          (mapv* inc [])))
    )

  (let [coll (range 1000)]
    (println "doall pmap")
    (user/quick-bench
      (doall (pmap inc coll)))
    (println "pmap*")
    (user/quick-bench
      (pmap* inc coll))
    )

  )
