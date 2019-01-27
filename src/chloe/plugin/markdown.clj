(ns chloe.plugin.markdown
  (:require [me.raynes.cegdown :as md]
            [clojure.string :as str]))

(defn markdown? [s] (re-find #".*\.md$" s))

(defn markdown [pages]
  (->> (map (fn [[path page]]
              (if (markdown? path)
                  [(str/replace path #"\.md$" ".html")
                   (assoc page :content (md/to-html (page :content) [:fenced-code-blocks :tables]))]
                  [path page]))
            pages)
       (into {})))
