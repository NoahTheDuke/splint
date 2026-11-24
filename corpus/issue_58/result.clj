(ns issue-58.example)

(defn a [x y]
  (when-not x y))

(defn b [v]
  (when-not (nil? v) (condp get v #{"null"} nil v)))
