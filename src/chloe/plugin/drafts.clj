(ns chloe.plugin.drafts)

(defn draft? [page]
  (not= true (get page :draft)))

(defn drafts
  "Remove draft pages."
  [site]
  (update site :partials #(filter draft? %)))
