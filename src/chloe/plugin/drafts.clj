(ns chloe.plugin.drafts)

(defn draft? [page]
  (not= true (get page :draft)))

(defn remove-drafts
  "Remove draft pages."
  [site]
  (update site :content #(filter draft? %)))
