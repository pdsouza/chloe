(ns chloe.plugin.drafts)

(defn remove-drafts
  "Remove draft pages."
  [pages]
  (->> pages
      (filter (fn [[path page]] (not= true (get page :draft))))
      (into {})))
