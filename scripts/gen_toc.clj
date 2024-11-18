(ns gen-toc
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]))

(doseq [f (fs/glob "docs/rules" "**.md")]
  (shell "markdown-toc -i --maxdepth 2" (str f)))
