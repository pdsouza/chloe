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

(defn content-to-page [file]
  {:content (slurp file)
   :modified (last-modified file)})

(defn asset-to-page [file]
  {:src-path (.getPath file)
   :modified (last-modified file)})

(defn content [dir]
  (->> dir
      (map-files (juxt #(relative-path-to dir (.getPath %)) content-to-page))
      (into {})))

(defn assets [dir]
  (->> dir
      (map-files (juxt #(relative-path-to dir (.getPath %)) asset-to-page))
      (into {})))

(defn export [export-dir pages]
  (doseq [[path page] pages]
    (let [file (if (str/ends-with? path "/")
                   (str export-dir path "index.html")
                   (str export-dir path))]
    (io/make-parents file)
    (if (contains? page :content) (spit file (page :content))
                                  (io/copy (io/file (page :src-path))
                                           (io/file file))))))

; ------------------------------------------------------------------------------
; ring

(defn find-page [pages uri]
  (let [search-keys (if (str/ends-with? uri "/")
                        [uri (str uri "index.html")]
                        [uri])]
    (-> pages
        (select-keys search-keys)
        ; TODO what if there are multiple entries? at least warn?
        (first)
        (nth 1))))

(defn serve [bf]
  (fn [request]
    (if-let [page (find-page (bf) (request :uri))]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (page :content)}
      {:status 404})))

(defn ring-serve [bf]
  "Returns a ring handler that serves pages from build function bf."
  (-> (serve bf)
      (wrap-resource "public")
      (wrap-content-type)))
