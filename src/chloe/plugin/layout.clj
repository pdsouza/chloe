(ns chloe.plugin.layout)

(defn find-layout [patterns path]
  (->> (filter (fn [pattern] (re-find (re-pattern pattern) path)) (keys patterns))
       (first)
       (get patterns)))

(defn select-layout [patterns path]
  (if-let [layout-fn (find-layout patterns path)]
    layout-fn
    identity))

(defn layout [patterns pages]
  (->> (map (fn [[path page]]
              (if (contains? page :content)
                  [path (assoc page :content ((select-layout patterns path) page))]
                  [path page]))
            pages)
        (into {})))
