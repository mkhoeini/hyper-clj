(ns hyper-clj.impl.actions
  (:require
    [hyper-clj.impl.render :refer [schedule-render]]))


(declare global-state)

(defn wire-state-to-actions
  [path state actions]
  (let [reduction-fn
        (fn [actions key action]
          (assoc! actions key
                  (if-not (fn? action)
                    (wire-state-to-actions (conj path key) (get state key) action)
                    (fn [data]
                      (let [state (get-in global-state path)
                            result (action data)
                            result (if (fn? result) (result state actions) result)]
                        (when (and result (not= result state) (nil? (.-then result)))
                          (set! global-state
                                (assoc-in global-state path (merge state result)))
                          (schedule-render))
                        result)))))]
    (->> actions
         (reduce-kv reduction-fn (transient {}))
         persistent!)))
