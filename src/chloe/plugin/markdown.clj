(ns chloe.plugin.markdown
  (:require [markdown.core :as markdown]
            [clojure.string :as str]))

(defn markdown? [s] (re-find #".*\.md$" s))

(defn markdown-page [page]
  (if (markdown? (page :url))
    (assoc page :url (str/replace (page :url) #"\.md$" ".html")
           :content (markdown/md-to-html-string (page :content)))
    page))

(defn markdown
  "Compile markdown content to HTML."
  [site]
  (update site :partials #(map markdown-page %)))
