; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.invalid-regex
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn invalid-regex?? [form]
  (or (not (string? form))
    (try
      (java.util.regex.Pattern/compile form)
      false
      (catch java.util.regex.PatternSyntaxException _ true))))

(defrule lint/invalid-regex
  "Invalid patterns in regex literals can lead to code not loading or crashing.

  @examples

  ; avoid
  #\"[a-z\"

  ; prefer
  #\"[a-z]\"
  "
  {:pattern '(splint/re-pattern (? pattern invalid-regex??))
   :ext #{:clj}
   :message "Invalid java.util.regex.Pattern"})
