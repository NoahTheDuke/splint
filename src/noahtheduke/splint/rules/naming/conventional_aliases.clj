; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.naming.conventional-aliases
  (:require
    [clojure.set :as set]
    [noahtheduke.splint.diagnostic :refer [->diagnostic]]
    [noahtheduke.splint.parser.ns :refer [deps-from-libspec]]
    [noahtheduke.splint.rules :refer [defrule]]
    [noahtheduke.splint.utils :refer [drop-quote]]))

(set! *warn-on-reflection* true)

(def library->alias
  '{clojure.core.async async
    clojure.core.matrix mat
    clojure.core.protocols p
    clojure.core.reducers r
    clojure.data.csv csv
    clojure.data.xml xml
    clojure.datafy datafy
    clojure.edn edn
    clojure.java.io io
    clojure.java.shell sh
    clojure.math math
    clojure.pprint pp
    clojure.set set
    clojure.spec.alpha s
    clojure.string str
    clojure.tools.cli cli
    clojure.tools.logging log
    clojure.walk walk
    clojure.zip zip})

(def alias->library
  '{async clojure.core.async
    cli clojure.tools.cli
    csv clojure.data.csv
    datafy clojure.datafy
    edn clojure.edn
    io clojure.java.io
    log clojure.tools.logging
    mat clojure.core.matrix
    math clojure.math
    p clojure.core.protocols
    pp clojure.pprint
    r clojure.core.reducers
    s clojure.spec.alpha
    set clojure.set
    sh clojure.java.shell
    str clojure.string
    walk clojure.walk
    xml clojure.data.xml
    zip clojure.zip})

(defn parse-require [libspecs]
  (into {}
        (comp (mapcat #(deps-from-libspec nil (drop-quote %)))
              (filter :alias)
              (map (juxt :ns :alias)))
        libspecs))

(defn- existing->expected-alias
  [aliases libspec lib]
  (let [existing (get aliases lib)
        expected (get library->alias lib)]
    ;; only care if libspec uses alias
    ;; and there's an anticipated alias
    ;; and they don't match
    (if (and existing expected (not= existing expected))
      (let [pairs (partition 2 (next libspec))]
        (->> pairs
             (mapcat (fn [pair]
                       (if (= :as (first pair))
                         [:as expected]
                         pair)))
             (cons lib)
             (vec)))
      libspec)))

(defn- rewrite-libspecs [aliases]
  (fn [libspec]
    ;; only [clojure.string ...]
    ;; not clojure.string or [clojure.string]
    (if (and (sequential? libspec)
             (< 1 (count libspec)))
      (let [lib (first libspec)]
        ;; [clojure.string :refer] or [clojure.string :as]
        (if (keyword? (second libspec))
          [(existing->expected-alias aliases libspec lib)]
          ;; [clojure pprint [string ...]]
          [(->> (next libspec)
                (map
                  (fn [subspec]
                    (if (vector? subspec)
                      (let [joined-lib (symbol (str lib "." (first subspec)))]
                        (if (keyword? (second subspec))
                          (let [new-spec (existing->expected-alias aliases subspec joined-lib)]
                            (assoc new-spec 0 (first subspec)))
                          subspec))
                      subspec)))
                (cons lib)
                vec)]))
      [libspec])))

(defn check-libspecs [libspecs]
  (when-let [aliases (not-empty (parse-require libspecs))]
    (let [existing-set (set (vals aliases))
          expected-aliases (select-keys library->alias (vec (keys aliases)))
          expected-set (set (vals expected-aliases))]
      (when (and (not-empty expected-set)
                 (not-empty (set/difference expected-set existing-set)))
        (->> libspecs
             (mapv (rewrite-libspecs aliases))
             (apply concat)
             (doall))))))

(comment
  (check-libspecs '(:require [clojure.string :as str]
                             clojure.set
                             [clojure.edn]
                             [clojure pprint [zip :refer [1 2 3] :as z]
                              [edn :as e]])))

(defn check-parent [ctx]
  (when-let [parent-form (:parent-form ctx)]
    (and (list? parent-form)
         (= 'ns (first parent-form)))))

(defrule naming/conventional-aliases
  "Through community and core practices over the years, various core libraries have gained standard or expected aliases. To better align with the community, it's best to use those aliases in favor of alternatives.

  Examples:

  ; bad
  (:require [clojure.string :as string])

  ; good
  (:require [clojure.string :as str])
  "
  {:pattern '(:require ?*args)
   :message "Prefer community standard aliases."
   :on-match (fn [ctx rule form _bindings]
               (when (check-parent ctx)
                 (when-let [replace-form (check-libspecs form)]
                   (->diagnostic ctx rule form {:replace-form replace-form}))))})
