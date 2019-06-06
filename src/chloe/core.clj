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

(defn content [dir]
  (map-files #(content-to-page dir %) dir))

(defn assets [dir]
  (map-files #(asset-to-page dir %) dir))

(defn ingest
  "Slurp in site content and assets."
  [site] (assoc site :content (content (site :content-path))
                     :assets  (assets (site :assets-path))))

(defn compl
  "Compose a seq of functions fs in left-to-right order."
  [& fs] (apply comp (reverse fs)))

(defn partialize-plugins
  "Prepare a seq of plugins ps for composition."
  [ps]
  (map (fn [[f & args]]
         (if args (apply partial f args) f))
       ps))

(defn compl-plugins
  "Compose a list of plugins left-to-right."
  [ps] (apply compl (partialize-plugins ps)))

(defn digest-content
  "Digest independent content. First pass of the digest cycle."
  [site]
  (let [process-plugins (compl-plugins (site :plugins))]
    (process-plugins site)))

(defn render-pages
  "Render top-level site pages."
  [site]
  (map (fn [[url, render-fn]] {:url url :content (render-fn site)})
       (site :pages)))

(defn digest-pages
  "Digest top-level content. Second pass of the digest cycle."
  [site]

  ; We need to do a little dance here since it is likely the
  ; existing content was already digested, and we don't want
  ; to digest them a second time. 
  
  (let [process-plugins (compl-plugins (site :plugins))
        prev-content (site :content)
        new-content ((process-plugins (assoc site :content (render-pages site))) :content)]
    (assoc site :content (concat prev-content new-content))))

(defn digest
  "Process site transforms."
  [site]

  ; Note that the digest cycle occurs in two passes: one for partial content
  ; and one for pages. This is because pages are the only content dependent on
  ; other site content (they are top-level: think a collection of blog posts for example) and need to
  ; be given pre-digested partial content for rendering (frontmatter should be parsed, etc.).

  (-> site
      (digest-content) ; pass 1
      (digest-pages))) ; pass 2

(defn gather-resources
  "Return a flat seq of all resources for a site."
  [site]
  (reduce concat (vals (select-keys site [:content :assets]))))

(defn export 
  "Spit out a site to :export-path."
  [site]
  (doseq [resource (gather-resources (-> site ingest digest))]
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
    (if-let [page (find-resource (-> site ingest digest) (request :uri))]
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
