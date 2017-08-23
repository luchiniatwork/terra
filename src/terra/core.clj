(ns terra.core
  (:require [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import java.io.File))

(defmacro defterra
  [name# form]
  `(def ~(vary-meta name# #(merge {:terraform true} %)) ~form))


(defn ^:private get-param [param]
  (cond
    (number? param) (str param)
    (keyword? param) (name param)))


(defn ^:private sanitize-params [args]
  (map #(if (string? %) (pr-str %) %) args))


(defmulti $form (fn [form args] (class form)))

(defmethod $form clojure.lang.Symbol
  [form args]
  (cond
    (nil? args) (str "${" form "}")
    (= 'get form) (str "${" (first args) "[" (get-param (second args)) "]}")
    :default (str "${" form "(" (apply str (interpose ", " (sanitize-params args))) ")}")))

(defmethod $form clojure.lang.Keyword
  [form [name#]]
  (str "${" name# "[" (name form) "]}"))

(defmethod $form clojure.lang.PersistentList
  [form _]
  ($form (first form) (rest form)))


(defmacro $
  [form]
  ($form form nil))

(defn generate []
  (println "Terra: generating...")
  (doseq [ns# (all-ns)]
    (doseq [[sym var] (ns-publics ns#)
            :when (:terraform (meta var))]
      (let [file-name (str "terraform"
                           File/separator
                           (ns-name ns#)
                           File/separator
                           sym
                           ".tf.json")
            f (io/file file-name)]
        (println "Terra:" file-name)
        (.mkdirs (.getParentFile f))
        (json/generate-stream (var-get var)
                              (io/writer f)))))
  (println "Terra: done!"))
