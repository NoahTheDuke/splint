(ns issue-58.example)

(defn a [x y]
  (if x nil y))

(defn b [v]
  (if (nil? v)
    nil
    (condp get v
      #{"null"} nil
      v)))
