; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.diagnostic
  "Namespace for all Diagnostics-related functionality.

  A Diagnostic is an instance of a match of a rule's pattern in a given
  analyzed code. It has the following definition:

  (defrecord Diagnostic
    [rule-name form message alt line column end-row end-col filename])")

(set! *warn-on-reflection* true)

(defrecord Diagnostic [rule-name form message alt line column end-row end-col filename])

(defn ->diagnostic
  "Create and return a new diagnostic."
  ([ctx rule form] (->diagnostic ctx rule form nil))
  ([ctx rule form {:keys [replace-form message filename form-meta]}]
   (let [form-meta (or form-meta (meta form))]
     (->Diagnostic
       (:full-name rule)
       form
       (or message (:message rule))
       replace-form
       (:line form-meta)
       (:column form-meta)
       (:end-row form-meta)
       (:end-col form-meta)
       (or filename (:filename ctx))))))

(comment
  (let [ctx {:filename "adsf"}
        rule {:full-name 'style/defn-fn
              :message "message"}
        form ^{:line 1 :column 2 :end-row 3 :end-col 4} '(1 2 3)
        opts {:message "other"}]
    (user/quick-bench
      (->diagnostic ctx rule form opts))))
