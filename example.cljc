(defn pick [d]
  (if (zero? d)
    #?(:clj :clj-branch :default :default-branch)
    d))
