(ns hyper-clj.impl.vdom
  (:require
    [clojure.spec.alpha :as s]
    [hyper-clj.spec.vdom :as vdom]
    [hyper-clj.spec.dom :as dom]
    [hyper-clj.impl.dom :refer [update-attrs]]))


(declare *global-state*)
(declare wired-actions)

(s/fdef fn->vdom
  :args (s/cat :node (s/or :fn fn? :vdom ::vdom/vdom))
  :ret ::vdom/vdom)

(defn fn->vdom
  [node]
  (let [vdom (if (fn? node)
               (node *global-state* wired-actions)
               node)]
    (or vdom "")))


(defn- create-text-node
  [text]
  (.createTextNode js/document text))

(defn- create-svg-node
  [tag]
  (.createElementNS js/document "http://www.w3.org/2000/svg" tag))

(defn- create-dom-node
  [tag]
  (.createElement js/document tag))

(s/fdef vdom->elem
  :args (s/cat :node ::vdom/vdom
               :svg? boolean?)
  :ret ::dom/element)

(declare *lifecycle*)
(defn vdom->elem
  [node svg?]
  (let [node-name (::vdom/name node)
        svg? (or svg? (= "svg" node-name))
        elem (cond
               (or (string? node) (number? node)) (create-text-node node)
               svg? (create-svg-node node-name)
               :else (create-dom-node node-name))
        attrs (::vdom/attrs node)]
    (when attrs
      (when-let [oncreate (:oncreate attrs)]
        (swap! *lifecycle* conj #(oncreate elem)))
      (doseq [child (::vdom/children node)]
        (.appendChild elem (vdom->elem (fn->vdom child))))
      (doseq [[name attr] attrs]
        (update-attrs elem name attr nil svg?)))
    elem))
