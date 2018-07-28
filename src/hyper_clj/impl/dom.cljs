(ns hyper-clj.impl.dom
  (:require
    [clojure.string :refer [lower-case]]
    [clojure.spec.alpha :as s]
    [hyper-clj.spec.dom :as dom]
    [hyper-clj.impl.dom-events :refer [get-event set-event remove-event]]))


; Make NodeList seqable
(extend-type js/NodeList
  ISeqable
  (-seq [this]
    (-> this
        js/Object.values
        (.reduce conj! (transient []))
        persistent!)))


(s/fdef recycle-elem
  :args (s/cat :elem ::dom/element))

(defn elem->vdom
  "Turn back a DOM element to a vdom node."
  [elem]
  (let [recycle-node #(if (= 3 (.-nodeType %))
                        (.-nodeValue %)
                        (elem->vdom %))]
    {::name (-> elem .-nodeName lower-case)
     ::attrs {}
     ::children (map recycle-node (.-childNodes elem))}))


(s/fdef set-style
  :args (s/cat :elem ::dom/element
               :attr string?
               :val (s/or :string string? :number number?))
  :ret nil?)

(defn set-style
  [elem attr val]
  (if (= \- (first attr))
    (.setProperty (.-style elem) attr val)
    (aset elem "style" attr val)))


(s/fdef update-attrs
  :args (s/cat :elem ::dom/element
               :name string?
               :val any?
               :old-val any?
               :svg? boolean?)
  :ret nil?)

(defn update-attrs
  [elem name val old-val svg?]
  (let [set-styles #(doseq [attr (keys (merge old-val val))
                            :let [v (get val attr "")]]
                      (set-style elem attr v))
        event? (and (= \o (first name)) (= \n (second name)))]
    (case name
      "key" nil
      "style" (set-styles)
      ; else
      (do
        (cond
          event?
          (let [event (drop 2 name)]
            (if val
              (set-event elem event val)
              (remove-event elem event)))

          (and (contains? elem name) (not= name "list") (not svg?))
          (aset elem name (or val ""))

          val
          (.setAttribute elem name val))
        (when-not val
          (.removeAttribute elem name))))))


(s/fdef update-elem
  :args (s/cat :elem ::dom/element
               :old-attrs map?
               :attrs map?
               :svg? boolean?)
  :ret nil?)

(defn update-elem
  [elem old-attrs attrs svg? recycling?]
  (doseq [name (keys (merge old-attrs attrs))
          :let [new-val (attrs name)
                old-val (if (#{"value" "checked"} name)
                          (aget elem name)
                          (old-attrs name))]]
    (when (not= new-val old-val)
      (update-attrs elem name new-val (old-attrs name) svg?)))
  (when-let [cb (get attrs (if recycling? :oncreate :onupdate))]
    (swap! *lifecycle* conj #(cb elem old-attrs))))
