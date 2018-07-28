(ns ^:figwheel-hooks hyper-clj.core
  (:require
    [hyper-clj.impl.app :refer [app]]
    [hyper-clj.impl.h :refer [h]]
    [hyper-clj.spec.action :as s.a]))


(enable-console-print!)


(def state {:count 0})

(def actions
  {:down (fn [{::s.a/keys [data state]}] (update state :count - data))
   :up (fn [{::s.a/keys [data state]}] (update state :count + data))})

(defn view
  [state dispatch]
  (h "div" {}
     [(h "h1" {} [(:count state)])
      (h "button" {:onclick #(dispatch [:down] 1)} ["-"])
      (h "button" {:onclick #(dispatch [:up] 1)} ["+"])]))


(app state actions view (js/document.querySelector "#app"))
