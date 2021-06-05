(ns icon-prep.core
  (:require [clojure.java.io :as io]))

(def start-dir (str (System/getProperty "user.home") "/Documents/svg-icons"))
(def end-dir (str (System/getProperty "user.home") "/Documents/blade-icons"))

(def file-names 
  (->> (io/file start-dir)
       file-seq
       (filter #(.isFile %))
       (mapv #(.getAbsolutePath %))))

(def file-contents (mapv slurp file-names))

(def file-contents-blade
  (->> 
    (map #(clojure.string/replace % #"(height|width|fill)=\"(#?[0-9A-Za-z]*)\"\s?" "") file-contents)
    (map #(clojure.string/replace-first % #">" " class=\"fill-current {{ \\$attributes->get('class', 'w-6 h-6 text-gray-900') }}\" {{ \\$attributes->filter(fn (\\$value, \\$key) => \\$key !== 'class') }}>"))))

(def file-names-blade
  (->> (mapv clojure.string/lower-case file-names)
       (map #(clojure.string/replace % #"\s+" "-"))
       (map #(clojure.string/replace % #"svg" "blade.php"))
       (map #(clojure.string/split % #"/"))
       (map last)))

(def file-names-no-ext
  (mapv #(str "icon." %)
       (map #(clojure.string/replace % #".blade.php" "") file-names-blade)))

(def name-contents (zipmap file-names-blade file-contents-blade))

(defn write-dir! []
  (cond
    (false? (.exists (io/file end-dir))) (.mkdir (io/file end-dir))
    :else (println "Directory already exists")))

(defn write-files! []
  (doall (map (fn [[key value]] (spit (str end-dir "/" key) value)) name-contents)))

(defn -main [& args]
  (write-dir!)
  (write-files!)
  (println file-names-no-ext)
  (println (count file-names-no-ext)))
