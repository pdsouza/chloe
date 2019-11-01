(ns chloe.plugin.markdown
  (:require [me.raynes.cegdown :as md]
            [clojure.string :as str]))

(defn markdown? [s] (re-find #".*\.md$" s))

(defn markdown-page [page]
  (if (markdown? (page :url))
    (assoc page :url (str/replace (page :url) #"\.md$" ".html")
                :content (md/to-html (page :content)
                                     [:fenced-code-blocks :tables]))
    page))

(defn markdown
  "Compile markdown content to HTML."
  [site]
  (update site :partials #(map markdown-page %)))
