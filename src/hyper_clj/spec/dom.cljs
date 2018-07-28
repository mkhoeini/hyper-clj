(ns hyper-clj.spec.dom
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::node #(instance? js/Node %))
(s/def ::element #(instance? js/Element %))
(s/def ::css-style-declaration #(instance? js/CSSStyleDeclaration %))
