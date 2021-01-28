(ns icon-prep.core
  (:require [clojure.java.io :as io]))

(def original-dir (io/file "/home/pb/Documents/figma-icons"))
(def end-dir "/home/pb/Documents/blade-icons")

(def file-names 
  (->> original-dir
       file-seq
       (filter #(.isFile %))
       (mapv #(.getAbsolutePath %))))

(def file-contents (mapv slurp file-names))

(def file-contents-clean
  (map #(clojure.string/replace-first % #">" " class=\"{{ \\$attributes->get('class', 'w6 h6 fill-current text-gray-900') }}>\"") 
    (map #(clojure.string/replace % #"(height|width)=\"([0-9]+)\"\s" "") file-contents)))

(def file-names-clean 
  (map last
       (map #(clojure.string/split % #"/") 
            (map #(clojure.string/replace % #"svg" "blade.php") 
                 (map #(clojure.string/replace % #"\s+" "-") 
                      (mapv clojure.string/lower-case file-names))))))

(def file-names-no-ext
  (mapv #(str "icon." %)
       (map #(clojure.string/replace % #".blade.php" "") file-names-clean)))

(def name-contents (zipmap file-names-clean file-contents-clean))

(defn write-dir! []
  (cond
    (false? (.exists (io/file end-dir))) (.mkdir (io/file end-dir))
    :else (println "Directory already exists")))

(defn write-files! []
  (doall (map (fn [[key value]] (spit (str end-dir "/" key) value)) name-contents)))

(defn -main []
  (write-dir!)
  (write-files!)
  (println file-names-no-ext))
