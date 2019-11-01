(ns chloe.plugin.pretty-urls
  (:require [clojure.string :as str]))

(defn prettify-path [path]
  (-> path
      (str/replace #"index\.html$" "")
      (str/replace #"\.html$" "/")))

(defn prettify-res [res]
  (update res :url #(prettify-path %)))

(defn prettify [res-map]
  (map prettify-res res-map))

(defn pretty-urls
  "Transform standard URLs to \"pretty\" URLs by rewriting file extensions.
  For example, /post/content.html -> /post/content/"
  [site]
  ; It is important to prettify partials first since pages may depend on their URLs
  ; to generate listings of content, e.g. a blog index.
  (-> site
      (update :partials prettify)
      (update :pages prettify)))
