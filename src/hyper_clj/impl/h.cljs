(ns hyper-clj.impl.h
  (:require
    [hyper-clj.spec.vdom :as vdom]
    [clojure.spec.alpha :as s]))


(s/fdef h
  :args (s/cat :name ::vdom/name
               :attrs ::vdom/attrs
               :children ::vdom/children)
  :ret ::vdom/vdom)

(defn h
      "Build the vdom."
      [name attrs children]
      (cond
        (keyword? name)
        {::name name
         ::attrs attrs
         ::children children
         ::key (::key attrs)}

        (fn? name)
        (name attrs children)

        :else (throw (ex-info "Expected keyword or fn." name))))
