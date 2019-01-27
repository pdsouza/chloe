(ns chloe.plugin.drafts)

(defn remove-drafts [pages]
  "Remove draft pages."
  (->> pages
      (filter (fn [[path page]] (not= true (get page :draft))))
      (into {})))
