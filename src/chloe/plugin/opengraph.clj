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
  [page base-url]
  (html/deftemplate template (java.io.StringReader. (page :content))
    []
    ; these are no-op for Enlive if the arg to append is nil
    ; so no need to check if the page contains the specified key
    [:head] (html/append (og-meta-tag "og:title" (page :title)))
    [:head] (html/append (og-meta-tag "og:description" (page :description)))
    ; og:image requires an absolute path
    [:head] (html/append (og-meta-tag "og:image" (when-let [img (page :image)]
                                                   (str base-url img))))
    [:head] (html/append (og-meta-tag "og:image:alt" (page :image-alt))))
  (assoc page :content (apply str (template))))

(defn opengraph
  "Add OpenGraph support to pages."
  [site]
  (update site :partials #(map (fn [page]
                                (if (html? (page :url)) 
                                  (inject-tags page (site :url))
                                  page))
                              %)))
