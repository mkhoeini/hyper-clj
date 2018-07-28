(ns hyper-clj.impl.dom-events)


(defprotocol EventContainer
  "Save the list of event listeners"
  (get-event [this ev])
  (set-event [this ev handler])
  (remove-event [this ev] [this ev handler]))


(defn- event-listener
  "General event listener for DOM events."
  [event]
  (let [events (.-currentTarget.events event)
        handler (get events (.-type event))]
    (handler event)))


(extend-type js/Element
  EventContainer
  (get-event [elem ev]
    (get (.-events elem) ev))

  (set-event [elem ev handler]
    (let [events (.-events elem)
          old-handler (get events ev)
          events (assoc events ev handler)]
      (aset elem "events" events)
      (when-not old-handler
        (.addEventListener elem ev event-listener))))

  (remove-event [elem ev]
    (.removeEventListener elem ev event-listener)
    (aset elem "events" (dissoc (.-events elem) ev))))
