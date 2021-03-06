(ns chloe.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]])
  (:require [chloe.plugin.markdown :refer [markdown]]
            [chloe.plugin.frontmatter :refer [frontmatter]]
            [chloe.plugin.drafts :refer [drafts]]
            [chloe.plugin.pages :refer [add-pages]]
            [chloe.plugin.pretty-urls :refer [pretty-urls]]
            [chloe.plugin.opengraph :refer [opengraph]]
            [chloe.plugin.layout :refer [layout]]))
; ------------------------------------------------------------------------------
; fs

(defn last-modified [file] (java.util.Date. (.lastModified file)))
(defn relative-path-to [dir path] (str/replace path dir ""))

(defn filter-dir
  "Filter all files in a directory, including subdirectory nodes."
  [f dir]
  (->> dir
       (io/as-file)
       (file-seq)
       (filter f)))

(defn filter-files [f dir]
  "Filter files in a directory."
  (->> dir
       (filter-dir #(.isFile %))
       (filter f)))

(defn map-files
  "Map files in a directory."
  [f dir]
  (->> dir
       (filter-dir #(.isFile %))
       (map f)))

(defn partial-to-res [root-dir file]
  {:url (relative-path-to root-dir (.getPath file))
   :content (slurp file)
   :modified (last-modified file)})

(defn asset-to-res [root-dir file]
  {:url (relative-path-to root-dir (.getPath file))
   :src-path (.getPath file)
   :modified (last-modified file)})

(defn path-to-lib [path]
  (-> path
      (str/replace #"\..*$" "")
      (str/replace "src/" "")
      (str/replace "/" ".")
      (str/replace "_" "-")))

(defn load-render-fn [lib]
  (require (symbol lib))
  (resolve (symbol (str lib "/render"))))

(defn unnsify [s]
  "Convert namespace-safe names back to normal."
  (str/replace s "_" "-"))

(defn clean [url]
  (-> url
      (str/replace #"\..*$" ".html")))

(defn clj-to-res [root-dir file]
  {:url (clean (unnsify (relative-path-to root-dir (.getPath file))))
   :modified (last-modified file)
   :render-fn (load-render-fn (-> file .getPath path-to-lib))})

(defn clj? [path] (re-find #".*\.clj$" path))

(defn slurp-pages [dir]
  (->> dir
       (filter-dir #(clj? (.getPath %)))
       (map #(clj-to-res dir %))))

(defn slurp-partials [dir]
  (->> dir
       (filter-files #(not (clj? (.getPath %))))
       (map #(partial-to-res dir %))))

(defn slurp-assets [dir]
  (map-files #(asset-to-res dir %) dir))

(defn render-pages [site]
  (update site :pages
    #(map (fn [page]
            (assoc page :content ((page :render-fn) site)))
          %)))

(defn gather-resources
  "Return a flat seq of all resources for a site."
  [site]
  (reduce concat (vals (select-keys site [:pages :partials :assets]))))

(defn project-pages-path
  "Return the project path where pages reside."
  [project-name]
  (str "src/" project-name "/page"))

(defn ingest [site]
  (assoc site :pages (slurp-pages (project-pages-path (site :project-name)))
              :partials (slurp-partials "resources/partials")
              :assets  (slurp-assets (site :asset-path))))

(defn compl [& fs]
  "Compose a seq of functions in left-to-right order."
  (apply comp (reverse fs)))

(defn partialize-plugins [ps]
  ; TODO multiple arg matches
  (map (fn [[f & args]] (if args (apply partial f args) f)) ps))

(defn compl-plugins [ps]
  (apply compl (partialize-plugins ps)))

(defn build [site]
  (->> site
       (ingest)
       (frontmatter)
       (markdown)
       (drafts)
       (pretty-urls)
       (render-pages)
       ((compl-plugins (site :plugins)))))

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
