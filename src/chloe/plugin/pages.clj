(ns chloe.plugin.pages)

(defn add-pages [new-pages site]
  (assoc site :pages (map (fn [[url, render-fn]]
                            {:url url :content (render-fn site)})
                          new-pages)))
