(ns chloe.plugin.pretty-urls
  (:require [clojure.string :as str]))

(defn prettify-urls
  "Transform standard URLs to \"pretty\" URLs by rewriting file extensions.
  For example, /post/content.html -> /post/content/index.html"
  [pages]
  (->> pages
       (map (fn [[path, page]]
              (if (contains? page :content) ; TODO only for HTML files
                  [(str/replace path #"\..*$" "/index.html") page]
                  [path page])))
       (into {})))
