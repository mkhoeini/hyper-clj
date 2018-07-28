(ns hyper-clj.impl.lifecycle
  (:require
    [clojure.spec.alpha :as s]
    [hyper-clj.spec.dom :as dom]
    [hyper-clj.spec.vdom :as vdom]
    [hyper-clj.impl.dom]))


(s/fdef remove-childs
  :args (s/cat :elem ::dom/element
               :node ::vdom/vdom)
  :ret nil?)

(defn remove-childs
  [elem node]
  (doseq [[child el] (zipmap (::vdom/children node) (.-childNodes elem))]
    (remove-childs el child))

  (when-let [ondestroy (-> node ::vdom/attrs :ondestroy)]
    (ondestroy elem)))


(s/fdef remove-elem
  :args (s/cat :parent ::dom/element
               :elem ::dom/element
               :node ::vdom/vdom)
  :ret nil?)

(defn remove-elem
  [parent elem node]
  (let [done #(do
                (remove-children elem node)
                (.removeChild parent elem))]
    (if-let [cb (get-in node [::vdom/attrs :onremove])]
      (cb elem done)
      (done))))
