; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.catch-throwable
  (:require
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/catch-throwable
  "Throwable is a superclass of all Errors and Exceptions in Java. Catching Throwable will also catch Errors, which indicate a serious problem that most applications should not try to catch. If there is a single specific Error you need to catch, use it directly.

  @examples

  ; avoid
  (try (foo)
    (catch Throwable t ...))

  ; prefer
  (try (foo)
    (catch ExceptionInfo ex ...)
    (catch AssertionError t ...))
  "
  {:pattern '(catch Throwable ?*args)
   :ext :clj
   :message "Throwable is too broad to safely catch."
   :replace '(catch Exception ?args)})
