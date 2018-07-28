(ns hyper-clj.impl.patch
  (:require
    [clojure.spec.alpha :as s]
    [hyper-clj.spec.vdom :as vdom]
    [hyper-clj.spec.dom :as dom]
    [hyper-clj.impl.lifecycle :refer [remove-elem]]
    [hyper-clj.impl.dom :refer [update-elem]]
    [hyper-clj.impl.vdom :refer [fn->vdom vdom->elem]]))


(declare *recycling?*)
(declare patch)

(defn- new-node
  [parent new-elem elem old-node]
  (.insertBefore parent new-elem elem)
  (when (some? old-node)
    (remove-elem parent elem old-node)))

(defn- update-node
  [svg? node old-node elem]
  (let [svg? (or svg? (= "svg" (:name node)))
        old-keyed (atom {})
        new-keyed (atom {})
        old-elems (atom [])
        old-children (:children old-node)
        children (:children node)]
    (update-elem elem (:attrs old-node) (:attrs node) svg?)
    (doseq [i (range (count old-children))]
      (swap! old-elems assoc i (aget elem "childNodes" i))
      (when-let [old-key (get-in old-children [i :key])]
        (swap! old-keyed assoc old-key [(@old-elems i) (old-children i)])))
    (loop [i 0
           k 0]
      (when (< k (count children))
        (let [old-key (get-in old-children [i :key])
              new-key (fn->vdom (children k))]
          (when (new-keyed old-key))
                ;(recur (inc i) k)
          (when (and (some? new-key) (= new-key (get-in old-children [(inc i) :key])))
            (when-not old-key
              (remove-elem elem (@old-elems i) (@old-children i))))
                ;(recur (inc i) k)
          (if (or (nil? new-key) *recycling?*)
            (if (nil? old-key)
              (do (patch elem (@old-elems i) (@old-children i) (children k) svg?)
                  (recur (inc i) (inc k)))
              (recur (inc i) k))
            (let [keyed-node (get @old-keyed new-key [])]
                 (if (= old-key new-key)
                   (do
                     (patch elem (first keyed-node) (second keyed-node) (children k) svg?)
                     (swap! new-keyed assoc new-key (children k))
                     (recur (inc i) (inc k)))
                   (do
                     (if (first keyed-node)
                       (patch elem
                              (.insertBefore elem (first keyed-node) (@old-elems i))
                              (keyed-node i)
                              (children k)
                              svg?)
                       (patch elem (@old-elems i) nil (children k) svg?))
                     (swap! new-keyed assoc new-key (children k))
                     (recur i (inc k)))))))))
    (doseq [ [i chld] (zipmap (range) @old-children) :when (nil? (get chld :key))]
      (remove-elem elem (@old-elems i) chld))
    (doseq [i @old-keyed]
      (when-not (@new-keyed i)
        (remove-elem elem (get-in @old-keyed [i 0]) (get-in @old-keyed [i 1]))))))

(s/fdef patch
  :args (s/cat :parent ::dom/element
               :elem ::dom/element
               :old-node ::vdom/vdom
               :node ::vdom/vdom
               :svg? boolean?)
  :ret nil?)

(defn patch
  [parent elem old-node node svg?]
  (cond
    (= node old-node)
    nil

    (or (nil? old-node) (not= (:name old-node) (:name node)))
    (new-node parent (vdom->elem node svg?) elem old-node)

    (nil? (:name old-node))
    (aset elem "nodeValue" node)

    :else
    (update-node svg? node old-node elem)))
