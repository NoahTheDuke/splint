; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.useless-catch
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/useless-catch
  "A `catch` clause that merely binds and rethrows the exception is a no-op, indicating a logical error of some kind.

  @examples

  ; avoid
  (try (do-stuff)
    (catch Exception ex
      (throw ex)))

  ; prefer
  (try (do-stuff)
    (catch Exception ex
      (log/error ex)
      (throw ex)))
  "
  {:pattern '(catch _ ?ex (throw ?ex))
   :message "Useless catch"
   :on-match (fn [ctx rule form {:syms [?ex]}]
               (when (not= '_ ?ex)
                 (->diagnostic ctx rule form)))})
