; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.lint.require-explicit-param-tags
  (:require
   [noahtheduke.splint.diagnostic :refer [->diagnostic]]
   [noahtheduke.splint.rules :refer [defrule]]))

(set! *warn-on-reflection* true)

(defn interop? [sym]
  (and (qualified-symbol? sym)
    (:splint/import-ns (meta sym))))

(defrule lint/require-explicit-param-tags
  "Uniform qualified method values are a new syntax for calling into java code.
  They must resolve to a single static or instance method and to help with
  that, a new metadata syntax can be used: `^[]` aka `^{:param-tags []}`. Types
  are specified with classes, each corrosponding to an argument in the target
  method: `(^[long String] SomeClass/someMethod 1 \"Hello world!\")`

  If `:param-tags` is left off of a method value, then the compiler treats it
  as taking no arguments (a 0-arity static method or a 1-arity instance method
  with the instance being the first argument). And an `_` can be used as
  a wild-card in the cases where there is only a single applicable method (no
  overloads).

  These last two features are where there can be trouble. If, for whatever
  reason, the Java library adds an overload on type, then both the lack of
  `:param-tags` and a wild-card can lead to ambiguity. This is a rare occurence
  but risky/annoying enough that it's better to be explicit overall.

  The styles are named after what they're looking for:

  * `:missing` checks that there exists a `:param-tags` on a method value.
  * `:wildcard` checks that there are no usages of `_` in an existing `:param-tags`.
  * `:both` checks both conditions.

  Examples:

  ; avoid (chosen style :both or :missing)
  (java.io.File/mkdir (clojure.java.io/file \"a\"))

  ; avoid (chosen style :both or :wildcard)
  (^[_ _] java.io.File/createTempFile \"abc\" \"b\")

  ; prefer (chosen style :both or :missing)
  (^[] java.io.File/mkdir (clojure.java.io/file \"a\"))

  ; prefer (chosen style :both or :wildcard (default))
  (^[String String] java.io.File/createTempFile \"abc\" \"b\")
  "
  {:pattern '(? ?sym interop?)
   :init-type :symbol
   :min-clojure-version {:major 1 :minor 12}
   :on-match (fn [ctx rule form {:syms [?sym]}]
               (let [m (meta form)
                     style (:chosen-style (:config rule))
                     msg (cond
                           (and (not (:param-tags m))
                             (#{:both :missing} style))
                           "Set explicit :param-tags on method values"
                           (and (some #{'_} (:param-tags m))
                             (#{:both :wildcard} style))
                           "Prefer explicit :param-tags on method values")]
                 (when msg
                   (->diagnostic ctx rule form {:message msg}))))})
