; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.diagnostic
  "Namespace for all Diagnostics-related functionality.

  A Diagnostic is an instance of a match of a rule's pattern in a given
  analyzed code. It has the following definition:

  (defrecord Diagnostic
    [rule-name form message alt line column end-row end-col filename])"
  (:require
    [noahtheduke.splint.replace :refer [revert-splint-reader-macros]]))

(defrecord Diagnostic [rule-name form message alt line column end-row end-col filename])

(defn ->diagnostic
  "Create and return a new diagnostic."
  ([rule form] (->diagnostic rule form nil))
  ([rule form {:keys [replace-form message filename] :as _opts}]
   (let [form-meta (meta form)
         message (or message (:message rule))]
     (->Diagnostic
       (:full-name rule)
       (revert-splint-reader-macros form)
       message
       (revert-splint-reader-macros replace-form)
       (:line form-meta)
       (:column form-meta)
       (:end-row form-meta)
       (:end-col form-meta)
       (or filename (:filename form-meta))))))
