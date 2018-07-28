(ns hyper-clj.impl.render
  (:require
    [goog.functions :as gfn]
    [hyper-clj.impl.vdom :refer [fn->vdom]]
    [hyper-clj.impl.patch :refer [patch]]))


(declare *recycling?*)

(defn render
  [view container root-elem old-node lifecycle]
  (let [node (fn->vdom view)
        root-elem (patch container root-elem old-node node false)]
    (set! *recycling?* false)
    (doseq [l lifecycle]
      (l))
    {:old-node node
     :root-elem root-elem
     :lifecycle []}))

(def schedule-render
  (gfn/debounce render 0))
