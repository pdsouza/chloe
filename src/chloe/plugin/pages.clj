(ns chloe.plugin.pages)

(defn add-pages [new-pages pages]
  (merge pages (zipmap (keys new-pages)
                       (map (fn [render] {:content (render pages)})
                            (vals new-pages)))))
