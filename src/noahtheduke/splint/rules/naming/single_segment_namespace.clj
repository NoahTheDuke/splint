; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.single-segment-namespace
  (:require
   [clojure.string :as str]
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn single-segment-ns? [sexp]
  (let [ns-str (str sexp)]
    (not (or (str/includes? ns-str ".")
           (#{"build" "user"} (str sexp))))))

(defrule naming/single-segment-namespace
  "Namespaces exist to disambiguate names. Using a single segment namespace puts you in direct conflict with everyone else using single segment namespaces, thus making it more likely you will conflict with another code base.

  Examples:

  ; avoid
  (ns simple)

  ; prefer
  (ns noahtheduke.simple)
  "
  {:pattern '(ns (? ns single-segment-ns?) ?*args)
   :on-match (fn [ctx rule form {:syms [?ns]}]
               (let [message
                     (format "%s is a single segment. Consider adding an additional segment." ?ns)]
                 (->diagnostic ctx rule form {:message message})))})
