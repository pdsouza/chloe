(ns chloe.plugin.frontmatter
  (:require [yaml.core :as yaml]))

(def frontmatter-yaml-regex #"(?:\A-{3}\s*([\s\S]*?)\s*-{3} *\s)?([\S\s]*)")

(defn split-frontmatter [content]
  (if (instance? String content)
    (let [matches (re-find frontmatter-yaml-regex content)]
      (subvec matches 1))))

(defn parse-frontmatter [frontmatter] (yaml/parse-string frontmatter))

(defn frontmatter [site]
  (->> (site :content)
       (map (fn [page]
            (let [[frontmatter content] (split-frontmatter (page :content))]
              (if (some? frontmatter)
                  (merge page (parse-frontmatter frontmatter) {:content content})
                  page))))
       (assoc site :content)))
