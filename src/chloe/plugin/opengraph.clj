(ns chloe.plugin.opengraph
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

(defn html?
  "Check if a path contains HTML content."
  [path]
  (or (str/ends-with? path "/")
      (str/ends-with? path ".html")))

(defn og-meta-tag [property content]
  (when content
   (html/html [:meta {:property property :content content}])))

(defn inject-tags
  "Inject OpenGraph tags into a page."
  [page]
  (html/deftemplate template (java.io.StringReader. (page :content))
    []
    ; these are no-op for Enlive if the arg to append is nil
    ; so no need to check if the page contains the specified key
    [:head] (html/append (og-meta-tag "og:title" (page :title)))
    [:head] (html/append (og-meta-tag "og:description" (page :description))))
  (assoc page :content (apply str (template))))

(defn opengraph
  "Add OpenGraph support to pages."
  [sitemap]
  (->> sitemap
       (map (fn [[path page]]
              (if (html? path) [path (inject-tags page)]
                               [path page])))
      (into {})))
