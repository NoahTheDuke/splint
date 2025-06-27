; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.no-catch
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defrule lint/no-catch
  "Try without a `catch` (or `finally`) clause is a no-op, and indicates something got changed or broken at some point.

  With the default style `:accept-finally`, both `catch` and `finally` clauses are counted to see if the `try` is a no-op. The style `:only-catch` can be used to raise a warning for `(try ... (finally ...))` forms with no `catch` clauses.

  @examples

  ; avoid
  (try (foo))

  ; avoid (chosen style :only-catch)
  (try (foo)
    (finally (bar)))

  ; prefer (chosen style :only-catch)
  (try (foo)
    (catch Exception ex
      ...))

  ; prefer (chosen style :accept-finally (default))
  (try (foo)
    (finally (bar)))

  (try (foo)
    (catch Exception ex
      ...))
  "
  {:pattern '(try ?+forms)
   :on-match (fn [ctx rule form {:syms [?forms]}]
               (let [chosen-style (:chosen-style (:config rule))
                     pred (if (= :accept-finally chosen-style)
                            #{'catch 'finally}
                            #{'catch})
                     msg (if (= :accept-finally chosen-style)
                           "Missing `catch` or `finally`."
                           "Missing `catch`.")]
                 (when-not (->> ?forms
                             (filter #(and (sequential? %) (pred (first %))))
                             (seq))
                   (->diagnostic ctx rule form {:message msg}))))})
