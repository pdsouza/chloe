(ns chloe.plugin.twitter-card
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

(defn html?
  "Check if a path contains HTML content."
  [path]
  (or (str/ends-with? path "/")
      (str/ends-with? path ".html")))

(defn twitter-meta-tag [property content]
  (when content
    (html/html [:meta {:name property :content content}])))

(defn inject-tags
  "Inject Twitter Card tags into a page."
  [page base-url]
  (html/deftemplate template (java.io.StringReader. (page :content))
    []
    [:head] (html/append (twitter-meta-tag "twitter:title"
                                           (page :title)))
    [:head] (html/append (twitter-meta-tag "twitter:description"
                                           (page :description)))
    ; twitter:image requires an absolute path
    [:head] (html/append (twitter-meta-tag "twitter:image"
                                           (when-let [img (page :image)]
                                             (str base-url img))))
    [:head] (html/append (twitter-meta-tag "twitter:image:alt"
                                           (page :image-alt)))
    [:head] (html/append (twitter-meta-tag "twitter:card"
                                           (if-let [card-type (page :twitter-card)]
                                             card-type
                                             "summary"))))
  (assoc page :content (apply str (template))))

(defn twitter-card
  "Add Twitter Card support to pages."
  [site]
  (update site :partials #(map (fn [page]
                                 (if (html? (page :url))
                                   (inject-tags page (site :url))
                                   page))
                               %)))
