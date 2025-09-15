(require '[clojure.java.io :as io])

(defn download-d3 []
  (with-open [in (io/input-stream "https://cdn.jsdelivr.net/npm/d3@7/dist/d3.min.js")
              out (io/output-stream "resources/d3.js")]
    (io/copy in out))
  (println "D3.js downloaded successfully"))

(download-d3)