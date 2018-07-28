(ns hyper-clj.spec.vdom)


(s/def ::name (s/or :tag keyword?
                    :comp fn?))
(s/def ::attrs map?)
(s/def ::children (s/coll-of ::vdom))
(s/def ::vdom (s/keys :req [::name ::attrs ::children ::key]))

