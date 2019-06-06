(ns chloe.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

; ------------------------------------------------------------------------------
; fs

(defn last-modified [file] (java.util.Date. (.lastModified file)))
(defn relative-path-to [dir path] (str/replace path dir ""))

(defn map-files
  "Map files in a directory."
  [f dir]
  (->> dir
       (io/as-file)
       (file-seq)
       (filter #(.isFile %))
       (map f)))

(defn content-to-page [root-dir file]
  {:url (relative-path-to root-dir (.getPath file))
   :content (slurp file)
   :modified (last-modified file)})

(defn asset-to-page [root-dir file]
  {:url (relative-path-to root-dir (.getPath file))
   :src-path (.getPath file)
   :modified (last-modified file)})

(defn slurp-content [dir]
  (map-files #(content-to-page dir %) dir))

(defn slurp-assets [dir]
  (map-files #(asset-to-page dir %) dir))

(defn ingest-content [dir site]
  (update site :content (fn [prev-content]
                          (concat prev-content (slurp-content dir)))))

(defn ingest-assets [dir site]
  (update site :assets (fn [prev-assets]
                          (concat prev-assets (slurp-assets dir)))))

(defn render-pages
  "Render site pages."
  [pages site]
  (map (fn [[url, render-fn]] {:url url :content (render-fn site)})
       pages))

(defn ingest-pages [pages site]
  (update site :pages #(concat % (render-pages pages site))))

(defn gather-resources
  "Return a flat seq of all resources for a site."
  [site]
  (reduce concat (vals (select-keys site [:content :assets :pages]))))

(defn build [site] ((site :build-fn) site))

(defn export 
  "Spit out a site to :export-path."
  [site]
  (doseq [resource (gather-resources (build site))]
    (let [path (resource :url)
          file (if (str/ends-with? path "/")
                   (str (site :export-path) path "index.html")
                   (str (site :export-path) path))]
    (io/make-parents file)
    (if (contains? resource :content) (spit file (resource :content))
                                      (io/copy (io/file (resource :src-path))
                                               (io/file file))))))

; ------------------------------------------------------------------------------
; ring

(defn find-resource [site uri]
  (let [resources (gather-resources site)
        search-urls (if (str/ends-with? uri "/")
                        #{uri (str uri "index.html")}
                        #{uri})]
    (first (filter (comp search-urls :url) resources))))

(defn serve [site]
  (fn [request]
    (if-let [page (find-resource (build site) (request :uri))]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (page :content)}
      {:status 404})))

(defn ring-serve
  "Returns a ring handler that serves a site."
  [site]
  (-> (serve site)
      (wrap-resource "public")
      (wrap-content-type)))
