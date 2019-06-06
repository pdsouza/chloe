(ns chloe.plugin.layout)

(defn find-layout [patterns path]
  (->> (filter (fn [pattern] (re-find (re-pattern pattern) path)) (keys patterns))
       (first)
       (get patterns)))

(defn do-layout [patterns resource]
  (if-let [layout-fn (find-layout patterns (resource :url))]
    (assoc resource :content (layout-fn resource))
    resource))

(defn layout [patterns site]
  (-> site
      (update :content #(map (fn [resource] (do-layout patterns resource)) %))
      (update :pages #(map (fn [resource] (do-layout patterns resource)) %))))
