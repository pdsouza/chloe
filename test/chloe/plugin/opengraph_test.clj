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
   :description "This page is about awesome stuff."})

(defn get-head [page]
  (-> page
      :content
      java.io.StringReader.
      html/html-resource
      (html/select [:head])))

(deftest t-inject-tags
  (testing "should inject opengraph tags based on page metadata into <head>"
    (let [head (-> t-page inject-tags get-head first)]
      (is (= 2 (count (html/select head [:meta]))))
      (is (= 1 (count (html/select head
                                   [[:meta
                                     (html/attr= :property "og:title")
                                     (html/attr= :content (t-page :title))]]))))
      (is (= 1 (count (html/select head
                                   [[:meta
                                     (html/attr= :property "og:description")
                                     (html/attr= :content (t-page :description))]])))))))
