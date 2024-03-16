; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns noahtheduke.splint.pipeline
  "A wacky version of a queue that holds two different 'queues'.

  Any number of items can be queued with `queue`, at once or in sequence.
  These are joined like Clojure's PersistentQueue.
  Then when `pop` is called, all of the items in :queue are added to the front
  of :steps, and the first item is returned.

  Example:
  (def p (make-pipeline))
  (-> (queue! p 1 2 3)
      (queue! p 4 5 6))
  ; =>
  "
  (:refer-clojure :exclude [peek pop]) 
  (:require
   [clojure.core :as cc])
  (:import
   [clojure.lang PersistentQueue]))

(set! *warn-on-reflection* true)

;; Ideally, this would be an implementation of IPersistentStack.
;; Regretfully, that doesn't work with babashka, so for now I'm re-implementing them.
;; At some point, I'll break from babashka, but not right now.
(defrecord Pipeline [steps queue])

(defn peek [p]
  (or (cc/peek (:queue p)) (cc/peek (:steps p))))

(defn pop [p]
  (->Pipeline (cc/pop (into (:queue p) (:steps p))) PersistentQueue/EMPTY))

(defn make-pipeline []
  (->Pipeline PersistentQueue/EMPTY PersistentQueue/EMPTY))

(defn queue
  "Add any number of items to pipeline's queue"
  [pipeline & items]
  (if (seq items)
    (update pipeline :queue #(apply conj % items))
    pipeline))

(comment
  (-> (make-pipeline)
    (queue 1 2 3)
    (queue 4 5 6)
    (prn)
    ; (pop)
    ; (queue 7 8 9)
    ; pop
    ))
