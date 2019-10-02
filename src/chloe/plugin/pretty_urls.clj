(ns chloe.plugin.pretty-urls
  (:require [clojure.string :as str]))

(defn prettify [path]
  (str/replace path #"\.html$" "/"))

(defn pretty-urls
  "Transform standard URLs to \"pretty\" URLs by rewriting file extensions.
  For example, /post/content.html -> /post/content/index.html"
  [site]
  (update site :content (fn [content]
                          (map (fn [resource]
                            (update resource :url #(prettify %)))
                                content))))
