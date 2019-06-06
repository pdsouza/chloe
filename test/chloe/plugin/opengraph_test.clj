(ns chloe.plugin.opengraph-test
  (:require [clojure.test :refer :all]
            [net.cgrand.enlive-html :as html]
            [chloe.plugin.opengraph :refer :all]))

(deftest t-html?
  (testing "should return true for paths ending in '/'"
    (is (= true (html? "/")))
    (is (= true (html? "/a/deeply/nested/path/to/content/")))
  (testing "should return true for paths ending in '.html'"
    (is (= true (html? "/index.html")))
    (is (= true (html? "/a/deeply/nested/path/to/content.html")))
  (testing "should not return true for non HTML files"
    (is (= false (html? "/assets/photo.jpg")))
    (is (= false (html? "/a/markdown/file.md"))))
  )))

(def t-page
  {:content "<!DOCTYPE html><html><head></head><body></body></html>"
   :title "An Awesome Page Title"
   :description "This page is about awesome stuff."
   :ogImage "/assets/photo.jpg"})

(def t-site
  {:url "https://foo.com"
   :content [t-page]})

(defn get-head [page]
  (-> page
      :content
      java.io.StringReader.
      html/html-resource
      (html/select [:head])))

(deftest t-inject-tags
  (testing "should inject opengraph tags based on page metadata into <head>"
    (let [head (-> t-page (inject-tags (t-site :url)) get-head first)]
      (is (= 3 (count (html/select head [:meta]))))
      (is (= 1 (count (html/select head
                                   [[:meta
                                     (html/attr= :property "og:title")
                                     (html/attr= :content (t-page :title))]]))))
      (is (= 1 (count (html/select head
                                   [[:meta
                                     (html/attr= :property "og:description")
                                     (html/attr= :content (t-page :description))]]))))
      (is (= 1 (count (html/select head
                                   [[:meta
                                     (html/attr= :property "og:image")
                                     ; og:image must be an absolute path
                                     (html/attr= :content (str (t-site :url) (t-page :ogImage)))]]))))))
  (testing "should not add opengraph tags for which no metadata exists"
    (let [head (-> t-page
                   (dissoc :title :description :ogImage)
                   (inject-tags (t-site :url))
                   (get-head)
                   (first))]
      (is (= 0 (count (html/select head [:meta])))))))
