(ns hyper-clj.spec.action
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::data any?)
(s/def ::state map?)
(s/def ::action-path (s/coll-of keyword?))
(s/def ::dispatch (s/fspec
                    :args (s/cat :action ::action-path :data ::data)
                    :ret nil?))
(s/def ::action-context (s/keys :req [::data ::state ::dispatch]))
(s/def ::action-handler (s/fspec
                          :args (s/cat :context ::action-context)
                          :ret map?))
(s/def ::actions
  (s/map-of keyword? (s/or :handler ::action-handler
                           :collection ::actions)))
