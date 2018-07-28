(ns hyper-clj.impl.app
  (:require
    [hyper-clj.impl.actions :refer [wire-state-to-actions]]
    [hyper-clj.impl.render :refer [schedule-render]]
    [hyper-clj.impl.dom :refer [elem->vdom]]))

(defn app
  "Mount the app."
  [state actions view container]
  (let [root-elem (some-> container .-children (aget 0))
        old-node (some-> root-elem elem->vdom)
        lifecycle []
        recycling? true
        global-state state
        wired-actions (wire-state-to-actions [] global-state actions)]
    (schedule-render)))
